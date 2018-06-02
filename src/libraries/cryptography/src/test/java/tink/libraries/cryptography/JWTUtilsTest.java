package se.tink.libraries.cryptography;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class JWTUtilsTest {
    private static final String PRIVATE_KEY = "-----BEGIN EC PRIVATE KEY-----\n"
            + "MIHbAgEBBEEVanejzVeLAKaLaXUZCCWWf63jHOZE1rps1q3fFvLj4RMSzX+g1fTB\n"
            + "fxXpMNbxN5vcvwOs7Drzufq9R5CzIz62IqAHBgUrgQQAI6GBiQOBhgAEAfgUTs9A\n"
            + "0DBte5Yk3OcoTb4IdbCXSDHWYtP9ZPGsdN3ImbpgVNAQ6jNP9TAFMq3pBXVxvi2d\n"
            + "KTPO64WDa3ES7tlkASa0uhC6/13STPn7cvu8rzZmeLKevnVzyMIvuOeM77o2LscW\n"
            + "jI+h8hn6KyVLrM4DgBDzj5dQGv93Np3AO5WTcZBZ\n"
            + "-----END EC PRIVATE KEY-----\n";
    private static final String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n"
            + "MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQB+BROz0DQMG17liTc5yhNvgh1sJdI\n"
            + "MdZi0/1k8ax03ciZumBU0BDqM0/1MAUyrekFdXG+LZ0pM87rhYNrcRLu2WQBJrS6\n"
            + "ELr/XdJM+fty+7yvNmZ4sp6+dXPIwi+454zvujYuxxaMj6HyGforJUuszgOAEPOP\n"
            + "l1Aa/3c2ncA7lZNxkFk=\n"
            + "-----END PUBLIC KEY-----\n";
    private static final String WRONG_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n"
            + "MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQBZ7z+8YoaUzUvWX5PNpfTbPd9JQWB\n"
            + "jENG8pjmW+86sQTBxE+PjjHkPDzgozoyofegq1VaIz7oy8CE1JFrv0JJm3QBdLO8\n"
            + "513N41HAhC3HPK01BedQov56BV4ELFlUIc3O4fTgRSD19hb3UyWoOr6RQ7ZFrZ0n\n"
            + "jBA0ZdHVL+70hioQbSw=\n"
            + "-----END PUBLIC KEY-----\n";

    private static final String CHALLENGE = "THIS IS RANDOM";

    @Test
    public void testReadChallenge() throws Exception {
        String jwtToken = JWTUtils.create(CHALLENGE, ECDSAUtils.getPrivateKey(PRIVATE_KEY));
        assertThat(JWTUtils.readChallenge(jwtToken)).isEqualTo(CHALLENGE);
    }

    @Test
    public void testSignAndVerify() throws Exception {
        String jwtToken = JWTUtils.create(CHALLENGE, ECDSAUtils.getPrivateKey(PRIVATE_KEY));
        VerificationStatus status = JWTUtils.verify(CHALLENGE, jwtToken, ECDSAUtils.getPublicKey(PUBLIC_KEY));

        assertThat(status).isEqualTo(VerificationStatus.VALID);
    }

   @Test
    public void testWrongKeySignAndVerify() throws Exception {
        String jwtToken = JWTUtils.create(CHALLENGE, ECDSAUtils.getPrivateKey(PRIVATE_KEY));
        VerificationStatus status = JWTUtils.verify(CHALLENGE, jwtToken, ECDSAUtils.getPublicKey(WRONG_PUBLIC_KEY));

        assertThat(status).isEqualTo(VerificationStatus.INVALID);
    }

    @Test
    public void testBadChallengeSignAndVerify() throws Exception {
        String jwtToken = JWTUtils.create("EVIL PAYLOAD", ECDSAUtils.getPrivateKey(PRIVATE_KEY));
        VerificationStatus status = JWTUtils.verify(CHALLENGE, jwtToken, ECDSAUtils.getPublicKey(PUBLIC_KEY));

        assertThat(status).isEqualTo(VerificationStatus.INVALID);
    }
}
