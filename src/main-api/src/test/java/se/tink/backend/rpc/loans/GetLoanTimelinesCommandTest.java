package se.tink.backend.rpc.loans;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class GetLoanTimelinesCommandTest {
    @Test
    public void testCorrectConstruction() {
        GetLoanTimelinesCommand command = new GetLoanTimelinesCommand("userId");

        assertThat(command.getUserId()).isEqualTo("userId");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullUserIdInput() {
        new GetLoanTimelinesCommand(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyUserIdInput() {
        new GetLoanTimelinesCommand("");
    }
}
