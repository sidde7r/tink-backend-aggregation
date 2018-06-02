package se.tink.backend.rpc.auth.otp;

import java.util.Optional;
import org.junit.Test;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.phonenumbers.InvalidPhoneNumberException;
import se.tink.libraries.validation.exceptions.InvalidLocaleException;
import static org.assertj.core.api.Assertions.assertThat;

public class InitiateSmsOtpCommandTest {

    @Test
    public void testCorrectConstruction() throws InvalidPhoneNumberException, InvalidLocaleException {
        User user = new User();

        InitiateSmsOtpCommand command = InitiateSmsOtpCommand.builder()
                .withPhoneNumber("+46709202541")
                .withLocale("sv_SE")
                .withUser(Optional.of(user))
                .withRemoteAddress(Optional.of("127.0.0.1"))
                .build();

        assertThat(command.getPhoneNumber()).isEqualTo("+46709202541");
        assertThat(command.getLocale()).isEqualTo("sv_SE");
        assertThat(command.getUser().get()).isEqualTo(user);
        assertThat(command.getRemoteAddress().get()).isEqualTo("127.0.0.1");
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingPhoneNumber() throws InvalidPhoneNumberException, InvalidLocaleException {
        InitiateSmsOtpCommand.builder()
                .withLocale("sv_SE")
                .build();
    }

    @Test(expected = InvalidPhoneNumberException.class)
    public void testInvalidPhoneNumber() throws InvalidPhoneNumberException, InvalidLocaleException {
        InitiateSmsOtpCommand.builder()
                .withPhoneNumber("ABCDEF12345")
                .withLocale("sv_SE")
                .build();
    }

    @Test(expected = InvalidLocaleException.class)
    public void testMissingLocale() throws InvalidPhoneNumberException, InvalidLocaleException {
        InitiateSmsOtpCommand.builder()
                .withPhoneNumber("+46709202541")
                .build();
    }

    @Test(expected = InvalidLocaleException.class)
    public void testInvalidLocale() throws InvalidPhoneNumberException, InvalidLocaleException {
        InitiateSmsOtpCommand.builder()
                .withPhoneNumber("+46709202541")
                .withLocale("svenska")
                .build();
    }

    @Test
    public void testFallbackToUserLocale() throws InvalidPhoneNumberException, InvalidLocaleException {
        User user = new User();
        UserProfile userProfile = new UserProfile();
        userProfile.setLocale("sv_SE");
        user.setProfile(userProfile);

        InitiateSmsOtpCommand command = InitiateSmsOtpCommand.builder()
                .withPhoneNumber("+46709202541")
                .withUser(Optional.of(user))
                .build();

        assertThat(command.getLocale()).isEqualTo("sv_SE");
    }

}
