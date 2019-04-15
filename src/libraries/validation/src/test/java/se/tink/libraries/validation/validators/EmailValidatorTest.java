package se.tink.libraries.validation.validators;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.libraries.validation.exceptions.InvalidEmailException;

public class EmailValidatorTest {

    @Test(expected = InvalidEmailException.class)
    public void testNullInput() throws InvalidEmailException {
        EmailValidator.validate(null);
    }

    @Test(expected = InvalidEmailException.class)
    public void testEmptyInput() throws InvalidEmailException {
        EmailValidator.validate("");
    }

    @Test(expected = InvalidEmailException.class)
    public void testInvalidEmail() throws InvalidEmailException {
        EmailValidator.validate("erik@erik");
    }

    @Test
    public void testValidEmail() throws InvalidEmailException {
        EmailValidator.validate("erik.pettersson@tink.se");
    }

    @Test
    public void testValidEmailWithAlias() throws InvalidEmailException {
        EmailValidator.validate("erik.pettersson+tink@tink.se");
    }

    @Test(expected = InvalidEmailException.class)
    public void testInvalidEmailDueToUppercase() throws InvalidEmailException {
        EmailValidator.validate("Erik.Pettersson@tink.se");
    }

    @Test
    public void testIsValid() throws InvalidEmailException {
        assertThat(EmailValidator.isValid("erik.pettersson@tink.se")).isTrue();
        assertThat(EmailValidator.isValid("sdftink.se")).isFalse();
    }
}
