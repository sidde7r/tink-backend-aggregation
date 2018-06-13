//package se.tink.backend.aggregation.nxgen.core.account;
//
//import java.util.List;
//import java.util.Map;
//import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
//import se.tink.backend.aggregation.rpc.AccountTypes;
//import se.tink.backend.core.Amount;
//import se.tink.libraries.account.AccountIdentifier;
//
//public class SavingsAccount extends TransactionalAccount {
//    private final Double interestRate;
//
//    private SavingsAccount(String name, String accountNumber, Amount balance, List<AccountIdentifier> identifiers,
//            String tinkId, String bankIdentifier, Double interestRate, HolderName holderName,
//            Map<String, String> temporaryStorage) {
//        super(name, accountNumber, balance, identifiers, tinkId, bankIdentifier, holderName, temporaryStorage);
//        this.interestRate = interestRate;
//    }
//
//    @Override
//    public AccountTypes getType() {
//        return AccountTypes.SAVINGS;
//    }
//
//    public Double getInterestRate() {
//        return this.interestRate;
//    }
//
//    public static Builder builder(String accountNumber, Amount balance) {
//        return new Builder(accountNumber, balance);
//    }
//
//    public static class Builder extends TransactionalAccount.Builder {
//        private Double interestRate;
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
//        @Override
//        public Builder setName(String name) {
//            return (Builder) super.setName(name);
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
//        public Builder setTemporaryStorage(Map<String, String> temporaryStorage) {
//            return (Builder) super.setTemporaryStorage(temporaryStorage);
//        }
//
//        @Override
//        public <T> Builder addToTemporaryStorage(String key, T value) {
//            return (Builder) super.addToTemporaryStorage(key, value);
//        }
//
//        @Override
//        public SavingsAccount build() {
//            return new SavingsAccount(getName(), getAccountNumber(), getBalance(), getIdentifiers(), getUniqueIdentifier(),
//                    getBankIdentifier(), getInterestRate(), getHolderName(), getTemporaryStorage());
//        }
//    }
//}
