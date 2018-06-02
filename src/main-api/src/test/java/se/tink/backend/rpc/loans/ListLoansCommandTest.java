package se.tink.backend.rpc.loans;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ListLoansCommandTest {
    @Test
    public void testCorrectConstruction() {
        ListLoansCommand command = new ListLoansCommand("userId");

        assertThat(command.getUserId()).isEqualTo("userId");
    }

    @Test(expected = IllegalStateException.class)
    public void testNullInput() {
        new ListLoansCommand(null);
    }

    @Test(expected = IllegalStateException.class)
    public void testEmptyInput() {
        new ListLoansCommand("");
    }
}
