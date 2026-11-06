package br.com.eterniaserver.ffut.domain.challenge.entities;

import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeAnswerEntity.ChallengeResultEntity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

class TestRunnerServiceTest {

    private static final String SUM = """
            public class Main {
                public int sum(int a, int b) {
                    return a + b;
                }
            }
            """;

    private static final String SUM_WITH_BUG = """
            public class Main {
                public int sum(int a, int b) {
                    return a - b;
                }
            }
            """;

    private static final String SUM_TESTS = """
            import org.junit.jupiter.api.Assertions;
            import org.junit.jupiter.api.Test;
            public class MainTest {
                @Test
                void testSum() {
                    Main main = new Main();
                    Assertions.assertEquals(2, main.sum(1, 1));
                }
                @Test
                void testSumReturnsZero() {
                    Main main = new Main();
                    Assertions.assertEquals(0, main.sum(0, 0));
                }
                @Test
                void testSumReturnNegative() {
                    Main main = new Main();
                    Assertions.assertEquals(-4, main.sum(-2, -2));
                }
            }
            """;

    private static ChallengeAnswerEntity answer(String shownCode, String evaluationCode) {
        ChallengeAnswerEntity answer = new ChallengeAnswerEntity();

        answer.setId("ASDAJSD2ACA9NMASD");
        answer.setUserId("ASDAJSD2ACadaDASFD");
        answer.setUsername("Yuri");
        answer.setChallengeId("ASDAJSD2ACDCafasA9NMASD");
        answer.setChallengeVersion(1);
        answer.setChallengeCode(shownCode);
        answer.setEvaluationCode(evaluationCode);
        answer.setUserTestCode(SUM_TESTS);

        return answer;
    }

    private static ChallengeResultEntity runAndScore(ChallengeAnswerEntity answer) {
        ProcessRunnerEntity runner = new ProcessRunnerEntity(answer);
        ResultCondenserEntity condenser = new ResultCondenserEntity(
                runner.getResultOutputPath(),
                runner.getJacocoOutputPath(),
                runner.getPitestOutputPath()
        );
        condenser.condenseResults();
        condenser.generateScore();
        return condenser.getResultModel();
    }

    @Test
    void testProcessSimpleTest() {
        ProcessRunnerEntity runner = new ProcessRunnerEntity(answer(SUM, null));

        File pitestExpected = new File(runner.getPitestOutputPath());
        File jacocoExpected = new File(runner.getJacocoOutputPath());
        File resultExpected = new File(runner.getResultOutputPath());

        Assertions.assertTrue(pitestExpected.exists());
        Assertions.assertTrue(jacocoExpected.exists());
        Assertions.assertTrue(resultExpected.exists());
    }

    @Test
    void testRunsAgainstEvaluationCodeInsteadOfShownCode() {
        // O aluno vê o código com defeito, mas os testes rodam contra a referência correta.
        ChallengeResultEntity result = runAndScore(answer(SUM_WITH_BUG, SUM));

        Assertions.assertEquals(0, result.getTestsFailed());
        Assertions.assertEquals(0, result.getTestsError());
        Assertions.assertTrue(result.getScore() > 70, "score=" + result.getScore());
    }

    @Test
    void testRunsAgainstShownCodeWithoutEvaluationCode() {
        // Controle: sem referência oculta, o código exibido (com defeito) é o avaliado.
        ChallengeResultEntity result = runAndScore(answer(SUM_WITH_BUG, null));

        Assertions.assertEquals(2, result.getTestsFailed());
        Assertions.assertTrue(result.getScore() <= 15, "score=" + result.getScore());
    }

}
