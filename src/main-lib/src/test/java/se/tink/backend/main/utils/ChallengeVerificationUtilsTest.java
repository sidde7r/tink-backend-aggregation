package se.tink.backend.main.utils;

import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import se.tink.backend.core.User;
import se.tink.backend.core.auth.AuthenticationStatus;
import se.tink.backend.core.auth.UserAuthenticationChallenge;
import se.tink.libraries.cryptography.ECDSAUtils;
import se.tink.libraries.cryptography.JWTUtils;
import se.tink.libraries.cryptography.VerificationStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ChallengeVerificationUtilsTest {
    private static final String USER_ID = "c1a8a157212240c4b9f170d122c4fc65";
    private static final String KEY_ID = "205fc3173d994b528ffee857bd1a084e";
    private static final String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n"
            + "MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQB+BROz0DQMG17liTc5yhNvgh1sJdI\n"
            + "MdZi0/1k8ax03ciZumBU0BDqM0/1MAUyrekFdXG+LZ0pM87rhYNrcRLu2WQBJrS6\n"
            + "ELr/XdJM+fty+7yvNmZ4sp6+dXPIwi+454zvujYuxxaMj6HyGforJUuszgOAEPOP\n"
            + "l1Aa/3c2ncA7lZNxkFk=\n"
            + "-----END PUBLIC KEY-----\n";
    private static final String PRIVATE_KEY = "-----BEGIN EC PRIVATE KEY-----\n"
            + "MIHbAgEBBEEVanejzVeLAKaLaXUZCCWWf63jHOZE1rps1q3fFvLj4RMSzX+g1fTB\n"
            + "fxXpMNbxN5vcvwOs7Drzufq9R5CzIz62IqAHBgUrgQQAI6GBiQOBhgAEAfgUTs9A\n"
            + "0DBte5Yk3OcoTb4IdbCXSDHWYtP9ZPGsdN3ImbpgVNAQ6jNP9TAFMq3pBXVxvi2d\n"
            + "KTPO64WDa3ES7tlkASa0uhC6/13STPn7cvu8rzZmeLKevnVzyMIvuOeM77o2LscW\n"
            + "jI+h8hn6KyVLrM4DgBDzj5dQGv93Np3AO5WTcZBZ\n"
            + "-----END EC PRIVATE KEY-----\n";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    User user;

    @Before
    public void setUp() {
        when(user.getId()).thenReturn(USER_ID);
    }

    @Test
    public void fromVerificationStatusToAuthenticationStatus_fullTest() {
        assertThat(
                ChallengeVerificationUtils.fromVerificationStatusToAuthenticationStatus(user, VerificationStatus.VALID))
                .isEqualTo(AuthenticationStatus.AUTHENTICATED);
        assertThat(ChallengeVerificationUtils
                .fromVerificationStatusToAuthenticationStatus(user, VerificationStatus.INVALID))
                .isEqualTo(AuthenticationStatus.AUTHENTICATION_ERROR);
        assertThat(ChallengeVerificationUtils
                .fromVerificationStatusToAuthenticationStatus(user, VerificationStatus.EXPIRED))
                .isEqualTo(AuthenticationStatus.AUTHENTICATION_ERROR);
    }

    @Test
    public void verifySignedChallenge_successful() throws IOException {
        String token = JWTUtils.create("foo", ECDSAUtils.getPrivateKey(PRIVATE_KEY));
        UserAuthenticationChallenge challenge = UserAuthenticationChallenge.create()
                .withChallenge("foo")
                .withKeyId(KEY_ID)
                .withUserId(USER_ID)
                .build();
        challenge.consume();

        AuthenticationStatus status = ChallengeVerificationUtils.verifySignedChallenge(user, token, challenge,
                ECDSAUtils.getPublicKey(PUBLIC_KEY));
        assertThat(status).isEqualTo(AuthenticationStatus.AUTHENTICATED);
    }

    @Test
    public void verifySignedChallenge_beforeConsumed() throws IOException {
        String token = JWTUtils.create("foo", ECDSAUtils.getPrivateKey(PRIVATE_KEY));
        UserAuthenticationChallenge challenge = UserAuthenticationChallenge.create()
                .withChallenge("foo")
                .withKeyId(KEY_ID)
                .withUserId(USER_ID)
                .build();

        AuthenticationStatus status = ChallengeVerificationUtils.verifySignedChallenge(user, token, challenge,
                ECDSAUtils.getPublicKey(PUBLIC_KEY));
        assertThat(status).isEqualTo(AuthenticationStatus.AUTHENTICATION_ERROR);
    }

    @Test
    public void verifySignedChallenge_consumeTwice() throws IOException {
        String token = JWTUtils.create("foo", ECDSAUtils.getPrivateKey(PRIVATE_KEY));
        UserAuthenticationChallenge challenge = UserAuthenticationChallenge.create()
                .withChallenge("foo")
                .withKeyId(KEY_ID)
                .withUserId(USER_ID)
                .build();

        challenge.consume();
        AuthenticationStatus status = ChallengeVerificationUtils.verifySignedChallenge(user, token, challenge,
                ECDSAUtils.getPublicKey(PUBLIC_KEY));
        assertThat(status).isEqualTo(AuthenticationStatus.AUTHENTICATED);

        challenge.consume();
        status = ChallengeVerificationUtils.verifySignedChallenge(user, token, challenge,
                ECDSAUtils.getPublicKey(PUBLIC_KEY));
        assertThat(status).isEqualTo(AuthenticationStatus.AUTHENTICATION_ERROR);
    }

}
