package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.tink.backend.core.UserOAuth2ClientPk;
import se.tink.backend.core.UserOAuth2ClientRole;

@Repository
public interface UserOAuth2ClientRoleRepository extends JpaRepository<UserOAuth2ClientRole, UserOAuth2ClientPk>,
        UserOAuth2ClientRoleRepositoryCustom {

}
