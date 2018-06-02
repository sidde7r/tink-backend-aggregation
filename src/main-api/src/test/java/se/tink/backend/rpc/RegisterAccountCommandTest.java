package se.tink.backend.rpc;

import org.junit.Test;
import se.tink.libraries.validation.exceptions.InvalidEmailException;
import se.tink.libraries.validation.exceptions.InvalidLocaleException;
import static org.assertj.core.api.Assertions.assertThat;

public class RegisterAccountCommandTest {
    @Test
    public void testNullEmail() throws InvalidEmailException, InvalidLocaleException {
        RegisterAccountCommand command = new RegisterAccountCommand("authentication_token", null, "sv_SE");

        assertThat(command.getEmail().isPresent()).isFalse();
    }

    @Test
    public void testEmptyEmail() throws InvalidEmailException, InvalidLocaleException {
        RegisterAccountCommand command = new RegisterAccountCommand("authentication_token", "", "sv_SE");

        assertThat(command.getEmail().isPresent()).isFalse();
    }

    @Test(expected = InvalidEmailException.class)
    public void testBadEmail() throws InvalidEmailException, InvalidLocaleException {
        new RegisterAccountCommand("authentication_token", "foo_bar_email", "sv_SE");
    }

    @Test(expected = InvalidLocaleException.class)
    public void testBadLocale() throws InvalidEmailException, InvalidLocaleException {
        new RegisterAccountCommand("authentication_token", "erik@tink.se", "bad_Locale");
    }

    @Test
    public void testCorrectConstruction() throws InvalidEmailException, InvalidLocaleException {
        RegisterAccountCommand command = new RegisterAccountCommand("authentication_token", "erik@tink.se", "nl_NL");

        assertThat(command.getEmail().orElse(null)).isEqualTo("erik@tink.se");
        assertThat(command.getAuthenticationToken()).isEqualTo("authentication_token");
        assertThat(command.getLocale().toString()).isEqualTo("nl_NL");
    }
}
