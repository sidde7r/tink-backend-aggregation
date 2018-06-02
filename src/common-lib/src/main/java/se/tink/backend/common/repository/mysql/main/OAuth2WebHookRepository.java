package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import se.tink.backend.core.oauth2.OAuth2WebHook;

@Repository
public interface OAuth2WebHookRepository extends JpaRepository<OAuth2WebHook, String> {
    List<OAuth2WebHook> findByUserIdAndClientId(String userId, String clientId);

    List<OAuth2WebHook> findByClientId(String clientId);

    @Transactional
    void deleteByUserId(String userId);

    List<OAuth2WebHook> findByClientIdAndGlobal(String clientId, boolean global);
}
