package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.tink.backend.core.auth.UserAuthenticationChallenge;

@Repository
public interface UserAuthenticationChallengeRepository extends JpaRepository<UserAuthenticationChallenge, String>, UserAuthenticationChallengeRepositoryCustom {
}
