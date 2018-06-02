package se.tink.backend.common.repository.mysql.main;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.tink.backend.core.oauth2.OAuth2Authorization;

@Repository
public interface OAuth2AuthorizationRepository extends JpaRepository<OAuth2Authorization, String> {
    public List<OAuth2Authorization> findByUserId(String id);

    public OAuth2Authorization findByRefreshToken(String refreshToken);

    public OAuth2Authorization findByAccessToken(String credentials);
}
