package br.com.eterniaserver.testrunner;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MainTest {

    @Test
    void testSum() {
        Main main = new Main();
        Assertions.assertEquals(2, main.sum(1, 1));
    }

    @Test
    void testSubtract() {
        Main main = new Main();
        Assertions.assertEquals(0, main.subtract(1, 1));
    }

    @Test
    void testMultiply() {
        Main main = new Main();
        Assertions.assertEquals(10, main.multiply(2, 5));
    }

    @Test
    void testDivide() {
        Main main = new Main();
        Assertions.assertEquals(5, main.divide(10, 2));
    }

}
