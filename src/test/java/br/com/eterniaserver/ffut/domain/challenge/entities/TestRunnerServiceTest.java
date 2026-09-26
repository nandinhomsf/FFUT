package br.com.eterniaserver.ffut.domain.challenge.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

class TestRunnerServiceTest {

    @Test
    void testProcessSimpleTest() {
        ChallengeAnswerEntity answer = new ChallengeAnswerEntity();

        answer.setId("ASDAJSD2ACA9NMASD");
        answer.setUserId("ASDAJSD2ACadaDASFD");
        answer.setUsername("Yuri");
        answer.setChallengeId("ASDAJSD2ACDCafasA9NMASD");
        answer.setChallengeVersion(1);
        answer.setChallengeCode("""
                public class Main {
                    public int sum(int a, int b) {
                        return a + b;
                    }
                }
                """);
        answer.setUserTestCode("""
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
                """);

        ProcessRunnerEntity runner = new ProcessRunnerEntity(answer);

        File pitestExpected = new File(runner.getPitestOutputPath());
        File jacocoExpected = new File(runner.getJacocoOutputPath());
        File resultExpected = new File(runner.getResultOutputPath());

        Assertions.assertTrue(pitestExpected.exists());
        Assertions.assertTrue(jacocoExpected.exists());
        Assertions.assertTrue(resultExpected.exists());
    }

}
