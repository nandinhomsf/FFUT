package br.com.eterniaserver.ffut.domain.challenge.repositories;

import br.com.eterniaserver.ffut.domain.challenge.entities.ChallengeEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeRepository extends MongoRepository<ChallengeEntity, String> {
}
