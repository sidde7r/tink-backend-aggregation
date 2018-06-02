package se.tink.backend.rpc.loans;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.core.Loan;

public class UpdateLoansCommand {
    private String userId;
    private String accountId;
    private Loan.Type loanType;
    private Double interest;
    private Double balance;

    public UpdateLoansCommand() {
    }

    public String getUserId() {
        return userId;
    }

    public Double getBalance() {
        return balance;
    }

    public Double getInterest() {
        return interest;
    }

    public String getAccountId() {
        return accountId;
    }

    public Loan.Type getLoanType() {
        return loanType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {
        private String userId;
        private String accountId;
        private Loan.Type loanType;
        private Double interest;
        private Double balance;

        public Builder withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder withAccountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder withLoanType(Loan.Type loanType) {
            this.loanType = loanType;
            return this;
        }

        public Builder withInterest(Double interest) {
            this.interest = interest;
            return this;
        }

        public Builder withBalance(Double balance) {
            this.balance = balance;
            return this;
        }

        public UpdateLoansCommand build() {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(userId), "UserId must not be null or empty");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(accountId), "AccountId must not be null or empty");

            UpdateLoansCommand command = new UpdateLoansCommand();
            command.userId = userId;
            command.accountId = accountId;
            command.loanType = loanType;
            command.interest = interest;
            command.balance = balance;

            return command;
        }
    }
}
