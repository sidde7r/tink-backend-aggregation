package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.auth.AuthenticationToken;

public interface AuthenticationTokenRepository
        extends JpaRepository<AuthenticationToken, String>, AuthenticationTokenRepositoryCustom {

}
