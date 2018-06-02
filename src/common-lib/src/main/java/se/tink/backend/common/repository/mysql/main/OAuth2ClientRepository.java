package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.tink.backend.core.oauth2.OAuth2Client;

@Repository
public interface OAuth2ClientRepository extends JpaRepository<OAuth2Client, String> {

}
