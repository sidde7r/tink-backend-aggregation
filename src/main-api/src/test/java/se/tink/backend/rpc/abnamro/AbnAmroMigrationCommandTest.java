package se.tink.backend.rpc.abnamro;

import java.util.Optional;
import org.junit.Test;
import se.tink.backend.core.User;
import se.tink.libraries.validation.exceptions.InvalidPin6Exception;
import static org.assertj.core.api.Assertions.assertThat;

public class AbnAmroMigrationCommandTest {
    @Test
    public void testCorrectConstruction() throws InvalidPin6Exception {
        AbnAmroMigrationCommand command = AbnAmroMigrationCommand.builder()
                .withPin6("121212")
                .withUser(new User())
                .withSmsOtpVerificationToken("sms-otp-token")
                .withRemoteAddress(Optional.of("127.0.0.1"))
                .withUserAgent("Tink Mobile/1.7.4 (iOS; 8.1, iPhone Simulator)")
                .withUserDeviceId("12345")
                .build();

        assertThat(command.getPin6()).isEqualTo("121212");
        assertThat(command.getSmsOtpVerificationToken()).isEqualTo("sms-otp-token");
        assertThat(command.getUserAgent()).isEqualTo("Tink Mobile/1.7.4 (iOS; 8.1, iPhone Simulator)");
        assertThat(command.getUserDeviceId()).isEqualTo("12345");
        assertThat(command.getRemoteAddress().orElse(null)).isEqualTo("127.0.0.1");
        assertThat(command.getUser()).isNotNull();
    }

    @Test(expected = NullPointerException.class)
    public void testMissingUser() throws InvalidPin6Exception {
        AbnAmroMigrationCommand.builder()
                .withPin6("121212")
                .withSmsOtpVerificationToken("sms-otp-token")
                .withUserAgent("Tink Mobile/1.7.4 (iOS; 8.1, iPhone Simulator)")
                .withUserDeviceId("12345")
                .withRemoteAddress(Optional.of("127.0.0.1"))
                .build();
    }

    @Test(expected = InvalidPin6Exception.class)
    public void testMissingPin6() throws InvalidPin6Exception {
        AbnAmroMigrationCommand.builder()
                .withPin6(null)
                .withSmsOtpVerificationToken("sms-otp-token")
                .withUserAgent("Tink Mobile/1.7.4 (iOS; 8.1, iPhone Simulator)")
                .withUserDeviceId("12345")
                .withRemoteAddress(Optional.of("127.0.0.1"))
                .withUser(new User())
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingSmsOtpToken() throws InvalidPin6Exception {
        AbnAmroMigrationCommand.builder()
                .withPin6("121212")
                .withSmsOtpVerificationToken(null)
                .withUserAgent("Tink Mobile/1.7.4 (iOS; 8.1, iPhone Simulator)")
                .withUserDeviceId("12345")
                .withRemoteAddress(Optional.of("127.0.0.1"))
                .withUser(new User())
                .build();
    }
}
