package br.com.eterniaserver.ffut.domain.challenge.services;

import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeAnswerEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ProcessRunnerEntity;
import br.com.eterniaserver.ffut.domain.challenge.entities.ResultCondenserEntity;

import org.springframework.stereotype.Service;

@Service
public class TestRunnerService {

    public ProcessRunnerEntity run(ChallengeAnswerEntity challengeAnswer) {
        return new ProcessRunnerEntity(challengeAnswer);
    }

    public ResultCondenserEntity condense(ProcessRunnerEntity processRunner) {
        return new ResultCondenserEntity(
                processRunner.getResultOutputPath(),
                processRunner.getJacocoOutputPath(),
                processRunner.getPitestOutputPath()
        );
    }
}
