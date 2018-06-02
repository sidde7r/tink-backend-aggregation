package se.tink.backend.rpc.auth;

import java.util.Optional;
import org.junit.Test;
import se.tink.backend.core.User;
import se.tink.libraries.validation.exceptions.InvalidPin6Exception;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdatePhoneNumberCommandTest {

    @Test
    public void testCorrectConstruction() throws InvalidPin6Exception {
        User user = new User();

        UpdatePhoneNumberCommand command = UpdatePhoneNumberCommand.builder()
                .withSmsOtpVerificationToken("token")
                .withSessionId(Optional.of("sessionId"))
                .withPin6("121212")
                .withRemoteAddress(Optional.of("127.0.0.1"))
                .withUser(user)
                .build();

        assertThat(command.getPin6()).isEqualTo("121212");
        assertThat(command.getSessionId().orElse(null)).isEqualTo("sessionId");
        assertThat(command.getSmsOtpVerificationToken()).isEqualTo("token");
        assertThat(command.getUser()).isEqualTo(user);
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingSmsOtpToken() throws InvalidPin6Exception {
        User user = new User();

        UpdatePhoneNumberCommand command = UpdatePhoneNumberCommand.builder()
                .withSmsOtpVerificationToken("")
                .withSessionId(Optional.of("sessionId"))
                .withPin6("121212")
                .withRemoteAddress(Optional.of("127.0.0.1"))
                .withUser(user)
                .build();

        assertThat(command.getPin6()).isEqualTo("121212");
        assertThat(command.getSessionId().orElse(null)).isEqualTo("sessionId");
        assertThat(command.getSmsOtpVerificationToken()).isEqualTo("token");
        assertThat(command.getUser()).isEqualTo(user);
    }
}
