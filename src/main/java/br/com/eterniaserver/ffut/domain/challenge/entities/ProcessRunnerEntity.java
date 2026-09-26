package br.com.eterniaserver.ffut.domain.challenge.entities;

import lombok.Getter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class ProcessRunnerEntity {

    private static final String POM_FILE = """
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

    private static final String MAVEN_COMMAND = "mvn clean package";
    private static final String PITEST_COMMAND = "mvn org.pitest:pitest-maven:mutationCoverage";

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
        printWriter.print(answer.getChallengeCode());
        printWriter.close();

        Path testPath = Files.createDirectory(sourcePath.resolve("test"));
        Path testJavaPath = Files.createDirectories(testPath.resolve("java"));
        Path testFilePath = Files.createFile(testJavaPath.resolve("MainTest.java"));
        File testFile = testFilePath.toFile();

        fileWriter = new FileWriter(testFile);
        printWriter = new PrintWriter(fileWriter);
        printWriter.print(answer.getUserTestCode());
        printWriter.close();

        ProcessBuilder builder = new ProcessBuilder().command("bash", "-c", "cd " + answerPath + " && " + MAVEN_COMMAND);
        Process process = builder.start();
        process.waitFor(120, TimeUnit.SECONDS);

        builder = new ProcessBuilder().command("bash", "-c", "cd " + answerPath + " && " + PITEST_COMMAND);
        process = builder.start();
        process.waitFor(120, TimeUnit.SECONDS);

        pitestOutputPath = answerPath + "/target/pit-reports/mutations.csv";
        jacocoOutputPath = answerPath + "/target/jacoco-report/jacoco.xml";
        resultOutputPath = answerPath + "/target/surefire-reports/MainTest.txt";
    }

}
