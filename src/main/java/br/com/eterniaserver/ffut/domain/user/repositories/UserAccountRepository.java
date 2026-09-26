package br.com.eterniaserver.ffut.domain.user.repositories;

import br.com.eterniaserver.ffut.domain.user.entities.UserAccountEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends MongoRepository<UserAccountEntity, String> {

    boolean existsByLogin(String login);

    Optional<UserAccountEntity> findByLogin(String login);

    void deleteByLogin(String login);

}
