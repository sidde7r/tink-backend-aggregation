package se.tink.libraries.validation.validators;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;
import se.tink.libraries.validation.exceptions.InvalidPin6Exception;

public class Pin6ValidatorTest {
    @Test(expected = InvalidPin6Exception.class)
    public void testNullInput() throws InvalidPin6Exception {
        Pin6Validator.validate(null);
    }

    @Test(expected = InvalidPin6Exception.class)
    public void testEmptyInput() throws InvalidPin6Exception {
        Pin6Validator.validate("");
    }

    @Test(expected = InvalidPin6Exception.class)
    public void test5Digits() throws InvalidPin6Exception {
        Pin6Validator.validate("13455");
    }

    @Test(expected = InvalidPin6Exception.class)
    public void test7Digits() throws InvalidPin6Exception {
        Pin6Validator.validate("1123457");
    }

    @Test(expected = InvalidPin6Exception.class)
    public void test6RepeatedDigits() throws InvalidPin6Exception {
        Pin6Validator.validate("111111");
    }

    @Test(expected = InvalidPin6Exception.class)
    public void test5RepeatedDigits() throws InvalidPin6Exception {
        Pin6Validator.validate("555554");
    }

    @Test(expected = InvalidPin6Exception.class)
    public void test4RepeatedDigits() throws Exception {
        Pin6Validator.validate("111122");
    }

    @Test
    public void test3RepeatedDigits() throws Exception {
        Pin6Validator.validate("111222");
    }

    @Test
    public void testSequentialNumbers() throws Exception {
        assertThatThrownBy(() -> Pin6Validator.validate("012345"))
                .isInstanceOf(InvalidPin6Exception.class);
        assertThatThrownBy(() -> Pin6Validator.validate("123456"))
                .isInstanceOf(InvalidPin6Exception.class);
        assertThatThrownBy(() -> Pin6Validator.validate("234567"))
                .isInstanceOf(InvalidPin6Exception.class);
        assertThatThrownBy(() -> Pin6Validator.validate("345678"))
                .isInstanceOf(InvalidPin6Exception.class);
        assertThatThrownBy(() -> Pin6Validator.validate("456789"))
                .isInstanceOf(InvalidPin6Exception.class);
        assertThatThrownBy(() -> Pin6Validator.validate("987654"))
                .isInstanceOf(InvalidPin6Exception.class);
        assertThatThrownBy(() -> Pin6Validator.validate("876543"))
                .isInstanceOf(InvalidPin6Exception.class);
        assertThatThrownBy(() -> Pin6Validator.validate("765432"))
                .isInstanceOf(InvalidPin6Exception.class);
        assertThatThrownBy(() -> Pin6Validator.validate("654321"))
                .isInstanceOf(InvalidPin6Exception.class);
        assertThatThrownBy(() -> Pin6Validator.validate("543210"))
                .isInstanceOf(InvalidPin6Exception.class);
    }

    @Test(expected = InvalidPin6Exception.class)
    public void testChangePin6_samePin() throws Exception {
        Pin6Validator.validateChange("121212", "121212");
    }

    @Test(expected = InvalidPin6Exception.class)
    public void testChangePin6_weakNewPin() throws Exception {
        Pin6Validator.validateChange("121212", "111111");
    }

    @Test
    public void testChangePin6_validChange() throws Exception {
        Pin6Validator.validateChange("121212", "121213");
    }
}
