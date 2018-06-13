//package se.tink.backend.aggregation.nxgen.core.account;
//
//import com.google.common.base.Preconditions;
//import java.util.List;
//import java.util.Map;
//import se.tink.backend.aggregation.rpc.AccountTypes;
//import se.tink.backend.core.Amount;
//import se.tink.libraries.account.AccountIdentifier;
//
//public class LoanAccount extends Account {
//    private final Double interestRate;
//    private final LoanDetails details;
//
//    private LoanAccount(String name, String accountNumber, Amount balance, List<AccountIdentifier> identifiers,
//            String tinkId, String bankIdentifier, Double interestRate, LoanDetails details,
//            Map<String, String> temporaryStorage) {
//        super(name, accountNumber, balance, identifiers, tinkId, bankIdentifier, null, temporaryStorage);
//        this.interestRate = interestRate;
//        this.details = details;
//    }
//
//    @Override
//    public AccountTypes getType() {
//        return AccountTypes.LOAN;
//    }
//
//    @Override
//    public Amount getBalance() {
//        return Amount.createFromAmount(super.getBalance()).orElseThrow(NullPointerException::new);
//    }
//
//    public Double getInterestRate() {
//        return this.interestRate;
//    }
//
//    public LoanDetails getDetails() {
//        return this.details;
//    }
//
//    public static Builder builder(String accountNumber, Amount balance) {
//        return new Builder(accountNumber, balance);
//    }
//
//    public static class Builder extends Account.Builder {
//        private Double interestRate;
//        private LoanDetails details;
//
//        private Builder(String accountNumber, Amount balance) {
//            super(accountNumber, balance);
//        }
//
//        public Double getInterestRate() {
//            return this.interestRate;
//        }
//
//        public Builder setInterestRate(Double interestRate) {
//            this.interestRate = interestRate;
//            return this;
//        }
//
//        public LoanDetails getDetails() {
//            return this.details != null ? this.details : LoanDetails.builder()
//                    .setName(getName())
//                    .setLoanNumber(getAccountNumber())
//                    .build();
//        }
//
//        public Builder setDetails(LoanDetails details) {
//            this.details = details;
//            return this;
//        }
//
//        @Override
//        public Builder setName(String name) {
//            return (Builder) super.setName(name);
//        }
//
//        @Override
//        public Amount getBalance() {
//            return ensureNegativeSign(super.getBalance());
//        }
//
//        @Override
//        public Builder addIdentifier(AccountIdentifier identifier) {
//            return (Builder) super.addIdentifier(identifier);
//        }
//
//        @Override
//        public Builder setUniqueIdentifier(String uniqueIdentifier) {
//            return (Builder) super.setUniqueIdentifier(uniqueIdentifier);
//        }
//
//        @Override
//        public Builder setBankIdentifier(String bankIdentifier) {
//            return (Builder) super.setBankIdentifier(bankIdentifier);
//        }
//
//        @Override
//        public LoanAccount build() {
//            return new LoanAccount(getName(), getAccountNumber(), getBalance(), getIdentifiers(), getUniqueIdentifier(),
//                    getBankIdentifier(), getInterestRate(), getDetails(), getTemporaryStorage());
//        }
//
//        @Override
//        public Builder setTemporaryStorage(Map<String, String> temporaryStorage) {
//            return (Builder) super.setTemporaryStorage(temporaryStorage);
//        }
//
//        @Override
//        public <T> Builder addToTemporaryStorage(String key, T value) {
//            return (Builder) super.addToTemporaryStorage(key, value);
//        }
//    }
//
//    private static Amount ensureNegativeSign(Amount amount) {
//        Preconditions.checkNotNull(amount);
//        Preconditions.checkNotNull(amount.getValue());
//        Preconditions.checkArgument(amount.getValue() <= 0);
//        return amount;
//    }
//}
