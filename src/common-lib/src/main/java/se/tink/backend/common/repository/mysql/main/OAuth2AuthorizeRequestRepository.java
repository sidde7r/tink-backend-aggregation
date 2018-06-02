package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.tink.backend.core.oauth2.OAuth2Authorization;
import se.tink.backend.core.oauth2.OAuth2AuthorizeRequest;

@Repository
public interface OAuth2AuthorizeRequestRepository extends JpaRepository<OAuth2AuthorizeRequest, String> {
    public OAuth2AuthorizeRequest findOneByUserIdAndClientId(String userId, String clientId);
}
