package se.tink.libraries.phonenumbers.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.libraries.phonenumbers.InvalidPhoneNumberException;

public class PhoneNumberUtilsTest {

    @Test
    public void normalizeTests() throws InvalidPhoneNumberException {
        // Swedish numbers
        assertThat(PhoneNumberUtils.normalize("+46709202541")).isEqualTo("+46709202541");
        assertThat(PhoneNumberUtils.normalize("+460709202541")).isEqualTo("+46709202541");
        assertThat(PhoneNumberUtils.normalize("+46 709 20 25 41")).isEqualTo("+46709202541");

        // Dutch numbers
        assertThat(PhoneNumberUtils.normalize("+310622574260")).isEqualTo("+31622574260");
        assertThat(PhoneNumberUtils.normalize("+31622574260")).isEqualTo("+31622574260");
        assertThat(PhoneNumberUtils.normalize("+31 6 22 57 42 60")).isEqualTo("+31622574260");
        assertThat(PhoneNumberUtils.normalize("+31 (0) 6 22 57 42 60")).isEqualTo("+31622574260");
        assertThat(PhoneNumberUtils.normalize("+31(0)622574260")).isEqualTo("+31622574260");
    }

    @Test(expected = InvalidPhoneNumberException.class)
    public void normalizeNullThrowsException() throws InvalidPhoneNumberException {
        PhoneNumberUtils.normalize(null);
    }

    @Test
    public void isValidTests() throws InvalidPhoneNumberException {
        // Correct swedish numbers
        assertThat(PhoneNumberUtils.isValid("+46709202541")).isTrue();
        assertThat(PhoneNumberUtils.isValid("+460709202541")).isTrue();

        // Incorrect swedish numbers
        assertThat(PhoneNumberUtils.isValid("0709202541"))
                .isFalse(); // Correct but must be with country prefix
        assertThat(PhoneNumberUtils.isValid("90000")).isFalse();

        // Correct dutch numbers
        assertThat(PhoneNumberUtils.isValid("+310622574260")).isTrue();
        assertThat(PhoneNumberUtils.isValid("+31622574260")).isTrue();

        // Incorrect dutch numbers
        assertThat(PhoneNumberUtils.isValid("+310622574260111")).isFalse();
        assertThat(PhoneNumberUtils.isValid("+31622574")).isFalse();
    }
}
