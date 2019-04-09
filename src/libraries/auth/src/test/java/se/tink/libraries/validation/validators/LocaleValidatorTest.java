package se.tink.libraries.validation.validators;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.libraries.validation.exceptions.InvalidLocaleException;

public class LocaleValidatorTest {

    @Test(expected = InvalidLocaleException.class)
    public void testNullInput() throws InvalidLocaleException {
        LocaleValidator.validate(null);
    }

    @Test(expected = InvalidLocaleException.class)
    public void testEmptyInput() throws InvalidLocaleException {
        LocaleValidator.validate("");
    }

    @Test(expected = InvalidLocaleException.class)
    public void testInvalidLocale() throws InvalidLocaleException {
        LocaleValidator.validate("SESE2");
    }

    @Test
    public void testValidLocale() throws InvalidLocaleException {
        LocaleValidator.validate("sv_SE");
        LocaleValidator.validate("us_US");
    }

    @Test
    public void testIsValid() throws InvalidLocaleException {
        assertThat(LocaleValidator.isValid("sv_SE")).isTrue();
        assertThat(LocaleValidator.isValid("NL")).isFalse();
    }
}
