package se.tink.backend.rpc.auth.abnamro;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class AbnAmroAuthenticationCommandTest {
    @Test
    public void testCorrectConstruction() {
        AbnAmroAuthenticationCommand command = AbnAmroAuthenticationCommand.builder()
                .withInternetBankingSessionToken("internet-banking-session-token")
                .withClientKey("clientKey")
                .withOauthClientId("oauthClientId")
                .build();

        assertThat(command.getInternetBankingSessionToken()).isEqualTo("internet-banking-session-token");
        assertThat(command.getClientKey()).isEqualTo("clientKey");
        assertThat(command.getOauthClientId()).isEqualTo("oauthClientId");
    }

    @Test(expected = IllegalStateException.class)
    public void testEmptySessionToken() {
        AbnAmroAuthenticationCommand.builder()
                .withInternetBankingSessionToken(null)
                .withClientKey("clientKey")
                .withOauthClientId("oauthClientId")
                .build();
    }
}
