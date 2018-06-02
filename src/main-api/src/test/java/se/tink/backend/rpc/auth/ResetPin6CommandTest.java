package se.tink.backend.rpc.auth;

import org.junit.Test;
import se.tink.libraries.validation.exceptions.InvalidPin6Exception;
import static org.assertj.core.api.Assertions.assertThat;

public class ResetPin6CommandTest {
    @Test
    public void testCorrectConstruction() throws InvalidPin6Exception {
        ResetPin6Command command = ResetPin6Command.builder()
                .withSmsOtpVerificationToken("token")
                .withPin6("121212")
                .withRemoteAddress("127.0.0.1")
                .withUserDeviceId("deviceId")
                .withClientKey("clientKey")
                .withOauthClientId("oauthClientId")
                .withUserAgent("Grip/4.0.0 (iOS; 8.1, iPhone Simulator)")
                .build();

        assertThat(command.getPin6()).isEqualTo("121212");
        assertThat(command.getSmsOtpVerificationToken()).isEqualTo("token");
        assertThat(command.getClientKey()).isEqualTo("clientKey");
        assertThat(command.getOauthClientId()).isEqualTo("oauthClientId");
        assertThat(command.getUserDeviceId()).isEqualTo("deviceId");
        assertThat(command.getUserAgent()).isEqualTo("Grip/4.0.0 (iOS; 8.1, iPhone Simulator)");
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingSmsOtpToken() throws InvalidPin6Exception {
        ResetPin6Command.builder()
                .withSmsOtpVerificationToken("")
                .withPin6("121212")
                .withRemoteAddress("127.0.0.1")
                .withClientKey("clientKey")
                .withOauthClientId("oauthClientId")
                .withUserDeviceId("deviceId")
                .withUserAgent("Grip/4.0.0 (iOS; 8.1, iPhone Simulator)")
                .build();
    }
}
