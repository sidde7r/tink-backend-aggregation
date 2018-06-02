package se.tink.backend.rpc.loans;

import org.junit.Test;
import se.tink.backend.core.Loan;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdateLoansCommandTest {

    @Test
    public void testCorrectConstruction() {
        UpdateLoansCommand command = UpdateLoansCommand.builder()
                .withUserId("userId")
                .withAccountId("accountId")
                .withLoanType(Loan.Type.MORTGAGE)
                .withInterest(0.0195)
                .withBalance(20000.0)
                .build();

        assertThat(command.getUserId()).isEqualTo("userId");
        assertThat(command.getAccountId()).isEqualTo("accountId");
        assertThat(command.getLoanType()).isEqualTo(Loan.Type.MORTGAGE);
        assertThat(command.getInterest()).isEqualTo(0.0195);
        assertThat(command.getBalance()).isEqualTo(20000.0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testNullUserIdInput() {
        UpdateLoansCommand.builder()
                .withUserId(null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyUserIdInput() {
        UpdateLoansCommand.builder()
                .withUserId("")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullAccountIdInput() {
        UpdateLoansCommand.builder()
                .withAccountId(null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyAccountIdInput() {
        UpdateLoansCommand.builder()
                .withAccountId("")
                .build();
    }
}
