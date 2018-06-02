package se.tink.backend.consent.core.cassandra;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.UUID;
import org.junit.Test;
import se.tink.backend.consent.core.Action;
import static org.assertj.core.api.Assertions.assertThat;

public class CassandraUserConsentTest {
    @Test
    public void testCreateWithBuilder() throws SignatureException {
        UUID userId = UUID.randomUUID();
        String username = "username";
        Action action = Action.ACCEPTED;

        CassandraConsent consent = CassandraConsent.builder()
                .withKey("Key")
                .withVersion("1.0.0")
                .withTitle("Title")
                .withBody("Body")
                .withLocale("nl_NL")
                .build();

        CassandraUserConsent userConsent = CassandraUserConsent.builder()
                .withUsername(username)
                .withUserId(userId)
                .withAction(action)
                .withConsent(consent)
                .withSignature(null) // No signature
                .build();

        assertThat(userConsent.getUserName()).isEqualTo(username);
        assertThat(userConsent.getUserId()).isEqualTo(userId);
        assertThat(userConsent.getAction()).isEqualTo(action.toString());

        assertThat(userConsent.getKey()).isEqualTo(consent.getKey());
        assertThat(userConsent.getVersion()).isEqualTo(consent.getVersion());
        assertThat(userConsent.getLocale()).isEqualTo(consent.getLocale());

        assertThat(userConsent.getId()).isNotNull();
        assertThat(userConsent.getTimestamp()).isNotNull();
        assertThat(userConsent.getSignature()).isNull(); // No signature
    }

    @Test
    public void testCreateWithSignature() throws Exception {
        UUID userId = UUID.randomUUID();
        String username = "username";
        Action action = Action.ACCEPTED;

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");

        keyGen.initialize(2048, new SecureRandom());

        KeyPair keyPair = keyGen.generateKeyPair();

        Signature signature = Signature.getInstance("SHA256withRSA");

        signature.initSign(keyPair.getPrivate());

        CassandraConsent consent = CassandraConsent.builder()
                .withKey("Key")
                .withVersion("1.0.0")
                .withTitle("Title")
                .withBody("Body")
                .withLocale("nl_NL")
                .build();

        CassandraUserConsent userConsent = CassandraUserConsent.builder()
                .withUsername(username)
                .withUserId(userId)
                .withAction(action)
                .withConsent(consent)
                .withSignature(signature)
                .build();

        assertThat(userConsent.getSignature()).isNotNull();

        // Verify with the public key
        signature.initVerify(keyPair.getPublic());
        assertThat(userConsent.hasValidSignature(signature)).isTrue();
    }
}
