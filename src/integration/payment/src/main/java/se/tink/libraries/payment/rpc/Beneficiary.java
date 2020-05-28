package se.tink.libraries.payment.rpc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.libraries.account.AccountIdentifier;

public class Beneficiary {
    private final String name;
    private final String accountNumber;
    private final AccountIdentifier.Type accountNumberType;

    private Beneficiary(Builder builder) {
        this.name = builder.name;
        this.accountNumber = builder.accountNumber;
        this.accountNumberType = builder.accountNumberType;
    }

    public static class Builder {
        private String name;
        private String accountNumber;
        private AccountIdentifier.Type accountNumberType;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withAccountNumber(String accountNumber) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(accountNumber));
            this.accountNumber = accountNumber;
            return this;
        }

        public Builder withAccountNumberType(AccountIdentifier.Type accountNumberType) {
            Preconditions.checkNotNull(accountNumberType);
            this.accountNumberType = accountNumberType;
            return this;
        }

        public Beneficiary build() {
            return new Beneficiary(this);
        }
    }
}
