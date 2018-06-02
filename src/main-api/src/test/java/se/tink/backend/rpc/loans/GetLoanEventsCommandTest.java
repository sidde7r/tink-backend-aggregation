package se.tink.backend.rpc.loans;

import org.junit.Test;
import se.tink.libraries.validation.exceptions.InvalidLocaleException;
import static org.assertj.core.api.Assertions.assertThat;

public class GetLoanEventsCommandTest {
    @Test
    public void testCorrectConstruction() throws InvalidLocaleException {
        GetLoanEventsCommand command = new GetLoanEventsCommand("userId", "sv_SE");

        assertThat(command.getUserId()).isEqualTo("userId");
        assertThat(command.getLocale()).isEqualTo("sv_SE");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullUserIdInput() throws InvalidLocaleException {
        new GetLoanEventsCommand(null, "sv_SE");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyUserIdInput() throws InvalidLocaleException {
        new GetLoanEventsCommand("", "sv_SE");
    }

    @Test(expected = InvalidLocaleException.class)
    public void testNullLocaleInput() throws InvalidLocaleException {
        new GetLoanEventsCommand("userId", null);
    }

    @Test(expected = InvalidLocaleException.class)
    public void testEmptyLoacaleInput() throws InvalidLocaleException {
        new GetLoanEventsCommand("userId", null);
    }

    @Test(expected = InvalidLocaleException.class)
    public void testInvalidLoacaleInput() throws InvalidLocaleException {
        new GetLoanEventsCommand("userId", "locale");
    }
}
