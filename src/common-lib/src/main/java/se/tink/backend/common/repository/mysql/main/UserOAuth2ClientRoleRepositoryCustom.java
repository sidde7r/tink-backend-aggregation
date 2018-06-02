package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import se.tink.backend.core.UserOAuth2ClientRole;

public interface UserOAuth2ClientRoleRepositoryCustom {

    void deleteByUserId(String userId);

    List<UserOAuth2ClientRole> findByUserId(String userId);
}
