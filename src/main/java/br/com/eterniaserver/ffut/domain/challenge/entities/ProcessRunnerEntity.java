package br.com.eterniaserver.ffut.domain.challenge.entities;

import lombok.Getter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ProcessRunnerEntity {

    static final String POM_FILE = """
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
              <modelVersion>4.0.0</modelVersion>
              <groupId>br.com.eterniaserver</groupId>
              <artifactId>TestRunner</artifactId>
              <version>1.0-SNAPSHOT</version>
              <properties>
                <maven.compiler.source>21</maven.compiler.source>
                <maven.compiler.target>21</maven.compiler.target>
                <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
              </properties>
              <dependencies>
                <dependency>
                  <groupId>org.junit.jupiter</groupId>
                  <artifactId>junit-jupiter-engine</artifactId>
                  <version>5.9.1</version>
                  <scope>test</scope>
                </dependency>
                <dependency>
                  <groupId>org.pitest</groupId>
                  <artifactId>pitest-junit5-plugin</artifactId>
                  <version>1.2.1</version>
                </dependency>
              </dependencies>
              <build>
                <plugins>
                  <plugin>
                    <groupId>org.jacoco</groupId>
                    <artifactId>jacoco-maven-plugin</artifactId>
                    <version>0.8.12</version>
                    <executions>
                      <execution>
                        <goals>
                          <goal>prepare-agent</goal>
                        </goals>
                      </execution>
                      <execution>
                        <id>report-csv</id>
                        <goals>
                          <goal>report</goal>
                        </goals>
                        <configuration>
                          <outputEncoding>UTF-8</outputEncoding>
                          <outputDirectory>${project.build.directory}/jacoco-report</outputDirectory>
                          <formats>
                            <format>XML</format>
                          </formats>
                        </configuration>
                        <phase>prepare-package</phase>
                      </execution>
                    </executions>
                  </plugin>
                  <plugin>
                    <groupId>org.pitest</groupId>
                    <artifactId>pitest-maven</artifactId>
                    <version>1.16.1</version>
                    <configuration>
                      <targetClasses>
                        <param>Main</param>
                      </targetClasses>
                      <targetTests>
                        <param>MainTest</param>
                      </targetTests>
                      <outputFormats>
                        <outputFormat>csv</outputFormat>
                      </outputFormats>
                    </configuration>
                  </plugin>
                </plugins>
              </build>
            </project>
    """;

    // prepare-package roda os testes e o relatório do JaCoCo sem montar o jar, que não é usado e
    // estourava a memória com a CPU limitada do Render.
    static final String MAVEN_COMMAND = "mvn clean prepare-package";
    static final String PITEST_COMMAND = "mvn org.pitest:pitest-maven:mutationCoverage";

    @Getter
    private String pitestOutputPath;
    @Getter
    private String jacocoOutputPath;
    @Getter
    private String resultOutputPath;

    private final ChallengeAnswerEntity answer;

    public ProcessRunnerEntity(ChallengeAnswerEntity answer) {
        this.answer = answer;

        try {
            run();
        } catch (IOException | InterruptedException ignored) { }
    }

    // Limites de memória para as JVMs filhas (Maven, surefire e o processo do PIT), que herdam
    // JAVA_TOOL_OPTIONS. Sem isso, a aplicação + Maven + PIT passam dos 512 MB do plano grátis do Render.
    // Medido com o desafio das Multas: pico de ~380 MB para ~220 MB, com os mesmos resultados.
    private static final Logger LOGGER = Logger.getLogger(ProcessRunnerEntity.class.getName());

    private static final String CHILD_JVM_OPTIONS = "-Xmx64m -Xss512k -XX:+UseSerialGC -XX:TieredStopAtLevel=1 "
            + "-XX:ReservedCodeCacheSize=24m -XX:MaxMetaspaceSize=80m";

    // O processo do próprio Maven (que também roda a análise do PIT) precisa de um pouco mais de heap;
    // MAVEN_OPTS vem depois de JAVA_TOOL_OPTIONS na linha de comando e prevalece só para ele.
    private static final String MAVEN_JVM_OPTIONS = "-Xmx128m";

    // Folga para a CPU de 0,1 do plano grátis do Render (simulado localmente: ~47 s por etapa).
    private static final long STEP_TIMEOUT_SECONDS = 300;

    private static final int LOG_TAIL_LINES = 30;

    private static void runStep(Path answerPath, String command, String logName) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("bash", "-c", command)
                .directory(answerPath.toFile())
                .redirectErrorStream(true)
                // A saída vai para arquivo: um pipe nunca lido pode encher e travar o processo.
                .redirectOutput(answerPath.resolve(logName).toFile());
        builder.environment().put("JAVA_TOOL_OPTIONS", CHILD_JVM_OPTIONS);
        builder.environment().put("MAVEN_OPTS", MAVEN_JVM_OPTIONS);

        long start = System.nanoTime();
        Process process = builder.start();
        boolean finished = process.waitFor(STEP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            // Sem isso, um processo que estourou o tempo continuava rodando junto com o próximo passo.
            process.descendants().forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
            process.waitFor(10, TimeUnit.SECONDS);
        }
        long seconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start);

        if (finished && process.exitValue() == 0) {
            LOGGER.info(() -> "Runner step '" + command + "' finished in " + seconds + "s");
            return;
        }

        // A saída das etapas só fica no disco; em caso de problema, o final dela vai para o log do servidor.
        String outcome = finished ? "failed with exit code " + process.exitValue() : "timed out";
        LOGGER.warning("Runner step '" + command + "' " + outcome + " after " + seconds + "s. Last lines:\n"
                + tail(answerPath.resolve(logName)));
    }

    private static String tail(Path log) {
        try {
            List<String> lines = Files.readAllLines(log);
            return String.join("\n", lines.subList(Math.max(0, lines.size() - LOG_TAIL_LINES), lines.size()));
        } catch (IOException | UncheckedIOException exception) {
            return "(could not read " + log + ": " + exception.getMessage() + ")";
        }
    }

    private void run() throws IOException, InterruptedException {
        Path answerPath = Files.createTempDirectory(answer.getId());

        Path pomPath = Files.createFile(answerPath.resolve("pom.xml"));
        File pomFile = pomPath.toFile();

        FileWriter fileWriter = new FileWriter(pomFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(POM_FILE);
        printWriter.close();

        Path sourcePath = Files.createDirectory(answerPath.resolve("src"));

        Path mainPath = Files.createDirectory(sourcePath.resolve("main"));
        Path mainJavaPath = Files.createDirectory(mainPath.resolve("java"));
        Path mainFilePath = Files.createFile(mainJavaPath.resolve("Main.java"));
        File mainFile = mainFilePath.toFile();

        fileWriter = new FileWriter(mainFile);
        printWriter = new PrintWriter(fileWriter);
        printWriter.print(answer.hasEvaluationCode() ? answer.getEvaluationCode() : answer.getChallengeCode());
        printWriter.close();

        Path testPath = Files.createDirectory(sourcePath.resolve("test"));
        Path testJavaPath = Files.createDirectories(testPath.resolve("java"));
        Path testFilePath = Files.createFile(testJavaPath.resolve("MainTest.java"));
        File testFile = testFilePath.toFile();

        fileWriter = new FileWriter(testFile);
        printWriter = new PrintWriter(fileWriter);
        printWriter.print(answer.getUserTestCode());
        printWriter.close();

        runStep(answerPath, MAVEN_COMMAND, "build.log");
        runStep(answerPath, PITEST_COMMAND, "pitest.log");

        pitestOutputPath = answerPath + "/target/pit-reports/mutations.csv";
        jacocoOutputPath = answerPath + "/target/jacoco-report/jacoco.xml";
        resultOutputPath = answerPath + "/target/surefire-reports/MainTest.txt";
    }

}
