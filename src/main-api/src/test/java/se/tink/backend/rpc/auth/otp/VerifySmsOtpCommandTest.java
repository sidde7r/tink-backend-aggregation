package se.tink.backend.rpc.auth.otp;

import java.util.Optional;
import org.junit.Test;
import se.tink.backend.core.User;
import se.tink.libraries.phonenumbers.InvalidPhoneNumberException;
import static org.assertj.core.api.Assertions.assertThat;

public class VerifySmsOtpCommandTest {

    @Test
    public void testCorrectConstruction() throws InvalidPhoneNumberException {
        User user = new User();

        VerifySmsOtpCommand command = VerifySmsOtpCommand.builder()
                .withUser(Optional.of(user))
                .withCode("121212")
                .withRemoteAddress(Optional.of("127.0.0.1"))
                .withSmsOtpVerificationToken("sms-otp-token")
                .build();

        assertThat(command.getCode()).isEqualTo("121212");
        assertThat(command.getUser().get()).isEqualTo(user);
        assertThat(command.getRemoteAddress().get()).isEqualTo("127.0.0.1");
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingCode() throws InvalidPhoneNumberException {
        VerifySmsOtpCommand.builder().withSmsOtpVerificationToken("sms-otp-token").build();
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingSmsOtpVerificationToken() throws InvalidPhoneNumberException {
        VerifySmsOtpCommand.builder().withCode("121212").build();
    }

}
