package br.com.eterniaserver.ffut.domain.challenge.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

class WarmupPomTest {

    // O Dockerfile aquece o cache do Maven com warmup/pom.xml; se ele divergir do POM do runner,
    // os plugins que faltarem são baixados a cada vez que o container acorda.
    @Test
    void warmupPomMatchesRunnerPom() throws Exception {
        String warmupPom = Files.readString(Path.of("warmup", "pom.xml"));

        Assertions.assertEquals(normalize(ProcessRunnerEntity.POM_FILE), normalize(warmupPom));
    }

    // Só a indentação pode diferir.
    private static String normalize(String xml) {
        return xml.strip().lines().map(String::strip).reduce("", (a, b) -> a + b + "\n");
    }
}
