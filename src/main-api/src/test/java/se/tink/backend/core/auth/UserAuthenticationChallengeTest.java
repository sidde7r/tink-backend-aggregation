package se.tink.backend.core.auth;

import org.junit.Test;
import se.tink.libraries.auth.ChallengeStatus;
import static org.assertj.core.api.Assertions.assertThat;

public class UserAuthenticationChallengeTest {
    private static final String USER_ID = "c1a8a157212240c4b9f170d122c4fc65";
    private static final String KEY_ID = "205fc3173d994b528ffee857bd1a084e";
    private static final String CHALLENGE = "challenge";

    private static UserAuthenticationChallenge createChallenge() {
        return UserAuthenticationChallenge.create()
                .withChallenge(CHALLENGE)
                .withKeyId(KEY_ID)
                .withUserId(USER_ID)
                .build();
    }

    @Test
    public void testBuilder_successful() {
        UserAuthenticationChallenge challenge = createChallenge();

        assertThat(challenge.getStatus()).isEqualTo(ChallengeStatus.VALID);
        assertThat(challenge.getUserId()).isEqualTo(USER_ID);
        assertThat(challenge.getChallenge()).isEqualTo(CHALLENGE);
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilder_noUserId() {
        UserAuthenticationChallenge.create()
                .withKeyId(KEY_ID)
                .withChallenge(CHALLENGE)
                .build();
    }

    @Test
    public void testConsume_consumeTwice() {
        UserAuthenticationChallenge challenge = createChallenge();

        assertThat(challenge.getStatus()).isEqualTo(ChallengeStatus.VALID);
        challenge.consume();
        assertThat(challenge.getStatus()).isEqualTo(ChallengeStatus.CONSUMED);
        challenge.consume();
        assertThat(challenge.getStatus()).isEqualTo(ChallengeStatus.INVALID);
    }

    @Test
    public void testConsume_consumeExpired() {
        UserAuthenticationChallenge challenge = createChallenge();

        challenge.expire();
        assertThat(challenge.getStatus()).isEqualTo(ChallengeStatus.VALID);
        challenge.consume();
        assertThat(challenge.getStatus()).isEqualTo(ChallengeStatus.EXPIRED);
    }
}
