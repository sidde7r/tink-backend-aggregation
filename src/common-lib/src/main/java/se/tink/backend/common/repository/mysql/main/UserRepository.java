package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.tink.backend.core.User;

@Repository
public interface UserRepository extends JpaRepository<User, String>, UserRepositoryCustom {

    User findOneByUsername(String username);

    long countByProfileMarket(String market);

    User findOneByNationalId(String nationalId);
}
