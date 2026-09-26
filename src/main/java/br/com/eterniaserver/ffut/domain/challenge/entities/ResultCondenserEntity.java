package br.com.eterniaserver.ffut.domain.challenge.entities;

import br.com.eterniaserver.ffut.domain.challenge.enums.MutationType;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeAnswerEntity.ChallengeResultEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeAnswerEntity.MutationResultEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeAnswerEntity.LineResultEntity;

import lombok.Getter;

import org.apache.commons.io.FileUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResultCondenserEntity {

    private static final Logger LOGGER = Logger.getLogger(ResultCondenserEntity.class.getName());

    private static final int TESTS_RUNS = 0;
    private static final int TESTS_FAILED = 1;
    private static final int TESTS_ERROR = 2;

    private static final int MUTATION_TYPE = 2;
    private static final int MUTATION_INFO = 3;
    private static final int MUTATION_LINE = 4;
    private static final int MUTATION_STATUS = 5;

    @Getter
    private final ChallengeResultEntity resultModel = new ChallengeResultEntity();

    private final String resultOutputPath;
    private final String jacocoOutputPath;
    private final String pitestOutputPath;

    public ResultCondenserEntity(String resultOutputPath,
                                 String jacocoOutputPath,
                                 String pitestOutputPath) {
        this.resultOutputPath = resultOutputPath;
        this.jacocoOutputPath = jacocoOutputPath;
        this.pitestOutputPath = pitestOutputPath;
    }

    public void condenseResults() {
        this.readDefaultTest();
        this.readJacocoCoverage();
        this.readPitestMutationData();
    }

    public void generateScore() {
        int totalTests = resultModel.getTestsSuccess() + resultModel.getTestsFailed() + resultModel.getTestsError();

        double testScore = totalTests > 0 ? (double) resultModel.getTestsSuccess() / totalTests : 0.0;

        if (resultModel.getTestsSuccess() == 0) {
            resultModel.setScore(0.0);
            return;
        }

        if (totalTests != resultModel.getTestsSuccess()) {
            resultModel.setScore(0.15 * testScore * 100);
            return;
        }

        double instructionScore = calculateCoverageScore(
                resultModel.getInstructionCoverage(),
                resultModel.getInstructionMissed()
        );

        double branchScore = calculateCoverageScore(resultModel.getBranchCoverage(), resultModel.getBranchMissed());

        double lineScore = calculateCoverageScore(resultModel.getLineCoverage(), resultModel.getLineMissed());

        double complexityScore = calculateCoverageScore(
                resultModel.getComplexityCoverage(),
                resultModel.getComplexityMissed()
        );

        double methodScore = calculateCoverageScore(resultModel.getMethodCoverage(), resultModel.getMethodMissed());

        double mutationScore = calculateMutationScore(resultModel.getMutationResults());

        double finalScore = (0.15 * testScore) +
                (0.05 * instructionScore) +
                (0.1 * branchScore) +
                (0.05 * lineScore) +
                (0.1 * complexityScore) +
                (0.05 * methodScore) +
                (0.5 * mutationScore);

        resultModel.setScore(100 * finalScore);
    }

    private double calculateCoverageScore(int covered, int missed) {
        int total = covered + missed;
        return total > 0 ? (double) covered / total : 1.0;
    }

    private double calculateMutationScore(List<MutationResultEntity> mutationResults) {
        if (mutationResults.isEmpty()) {
            return 0.0;
        }

        double totalWeight = 0.0;
        double weightedKilledCount = 0.0;

        for (MutationResultEntity mutation : mutationResults) {
            double weight = getMutationWeight(mutation.getMutationType());
            totalWeight += weight;
            if (mutation.getIsKilled()) {
                weightedKilledCount += weight;
            }
        }

        return totalWeight > 0 ? weightedKilledCount / totalWeight : 0.0;
    }

    private double getMutationWeight(MutationType mutationType) {
        return switch (mutationType) {
            case CONDITIONAL_BOUNDARY -> 1.0;
            case NEGATE_CONDITIONALS -> 0.9;
            case MATH -> 0.8;
            case INCREMENTS, INVERT_NEGATIVES -> 0.7;
            case VOID_METHOD_CALLS -> 0.6;
            case EMPTY_RETURNS, NULL_RETURNS -> 0.5;
            case PRIMITIVE_RETURNS -> 0.4;
            case TRUE_RETURNS, FALSE_RETURNS -> 0.3;
        };
    }

    private void readPitestMutationData() {
        File file = new File(pitestOutputPath);

        List<MutationResultEntity> mutationResults = new ArrayList<>();

        try {
            List<String> lines = FileUtils.readLines(file, StandardCharsets.UTF_8);

            for (String line : lines) {
                if (line.isEmpty()) {
                    break;
                }

                String[] data = line.split(",");

                MutationResultEntity mutationResult = new MutationResultEntity();

                mutationResult.setMutationType(MutationType.getEnum(data[MUTATION_TYPE]));
                mutationResult.setMutationInfo(data[MUTATION_INFO]);
                mutationResult.setIsKilled(data[MUTATION_STATUS].equals("KILLED"));
                mutationResult.setLine(parseInt(data[MUTATION_LINE]));

                mutationResults.add(mutationResult);
            }
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, exception.getMessage());
        }

        resultModel.setMutationResults(mutationResults);
    }

    private void readJacocoCoverage() {
        File file = new File(jacocoOutputPath);

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

            documentBuilderFactory.setValidating(false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document jacocoReport = documentBuilder.parse(file);

            jacocoReport.getDocumentElement().normalize();

            NodeList sourceFiles = jacocoReport.getElementsByTagName("sourcefile");

            for (int i = 0; i < sourceFiles.getLength(); i++) {
                Node sourceFileNode = sourceFiles.item(i);

                if (sourceFileNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element sourceFileElement = (Element) sourceFileNode;

                    NodeList lines = sourceFileElement.getElementsByTagName("line");

                    List<LineResultEntity> lineResults = new ArrayList<>(lines.getLength());
                    for (int j = 0; j < lines.getLength(); j++) {
                        lineResults.add(getLineResultEntity(lines.item(j)));
                    }

                    resultModel.setLineResults(lineResults);

                    NodeList counters = sourceFileElement.getElementsByTagName("counter");
                    for (int j = 0; j < counters.getLength(); j++) {
                        extracted(counters.item(j));
                    }
                }
            }

        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, exception.getMessage());
        }
    }

    private void extracted(Node counterNode) {
        Element counterElement = (Element) counterNode;

        String type = counterElement.getAttribute("type");
        String missed = counterElement.getAttribute("missed");
        String covered = counterElement.getAttribute("covered");

        switch (type) {
            case "INSTRUCTION":
                resultModel.setInstructionCoverage(parseInt(covered));
                resultModel.setInstructionMissed(parseInt(missed));
                break;
            case "BRANCH":
                resultModel.setBranchCoverage(parseInt(covered));
                resultModel.setBranchMissed(parseInt(missed));
                break;
            case "LINE":
                resultModel.setLineCoverage(parseInt(covered));
                resultModel.setLineMissed(parseInt(missed));
                break;
            case "COMPLEXITY":
                resultModel.setComplexityCoverage(parseInt(covered));
                resultModel.setComplexityMissed(parseInt(missed));
                break;
            case "METHOD":
                resultModel.setMethodCoverage(parseInt(covered));
                resultModel.setMethodMissed(parseInt(missed));
                break;
            default:
                break;
        }
    }

    private void readDefaultTest() {
        File file = new File(resultOutputPath);

        try {
            String[] data = FileUtils
                    .readLines(file, StandardCharsets.UTF_8)
                    .get(3)
                    .split(", ");

            resultModel.setTestsSuccess(getTestValue(data, TESTS_RUNS));
            resultModel.setTestsFailed(getTestValue(data, TESTS_FAILED));
            resultModel.setTestsError(getTestValue(data, TESTS_ERROR));
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, exception.getMessage());

            resultModel.setTestsSuccess(0);
            resultModel.setTestsFailed(0);
            resultModel.setTestsError(0);
        }
    }

    private LineResultEntity getLineResultEntity(Node lineNode) {
        Element lineElement = (Element) lineNode;

        LineResultEntity lineResult = new LineResultEntity();
        lineResult.setLineNumber(parseInt(lineElement.getAttribute("nr")));
        lineResult.setInstructionMissed(parseInt(lineElement.getAttribute("mi")));
        lineResult.setInstructionCoverage(parseInt(lineElement.getAttribute("ci")));
        lineResult.setBranchMissed(parseInt(lineElement.getAttribute("mb")));
        lineResult.setBranchCoverage(parseInt(lineElement.getAttribute("cb")));

        return lineResult;
    }

    private int getTestValue(String[] data, int index) {
        return parseInt(data[index].split(": ")[1].trim());
    }

    private int parseInt(String value) {
        return Integer.parseInt(value);
    }

}
