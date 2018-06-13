//package se.tink.backend.aggregation.nxgen.core.account;
//
//import java.util.List;
//import java.util.Map;
//import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
//import se.tink.backend.aggregation.rpc.AccountTypes;
//import se.tink.backend.core.Amount;
//import se.tink.libraries.account.AccountIdentifier;
//
//public class CreditCardAccount extends Account {
//    private final Amount availableCredit;
//
//    private CreditCardAccount(String name, String accountNumber, Amount balance, List<AccountIdentifier> identifiers,
//            String tinkId, String bankIdentifier, Amount availableCredit, HolderName holderName,
//            Map<String, String> temporaryStorage) {
//        super(name, accountNumber, balance, identifiers, tinkId, bankIdentifier, holderName, temporaryStorage);
//        this.availableCredit = availableCredit;
//    }
//
//    public Amount getAvailableCredit() {
//        return new Amount(this.availableCredit.getCurrency(), this.availableCredit.getValue());
//    }
//
//    @Override
//    public AccountTypes getType() {
//        return AccountTypes.CREDIT_CARD;
//    }
//
//    @Override
//    public se.tink.backend.aggregation.rpc.Account toSystemAccount() {
//        se.tink.backend.aggregation.rpc.Account account = super.toSystemAccount();
//
//        account.setAvailableCredit(this.availableCredit.getValue());
//
//        return account;
//    }
//
//    public static Builder builder(String accountNumber, Amount balance, Amount availableCredit) {
//        return new Builder(accountNumber, balance, availableCredit);
//    }
//
//    public static class Builder extends Account.Builder {
//        private final Amount availableCredit;
//
//        private Builder(String accountNumber, Amount balance, Amount availableCredit) {
//            super(accountNumber, balance);
//            this.availableCredit = availableCredit;
//        }
//
//        public Amount getAvailableCredit() {
//            return Amount.createFromAmount(this.availableCredit).orElseThrow(NullPointerException::new);
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
//        public Builder setHolderName(HolderName holderName) {
//            return (Builder) super.setHolderName(holderName);
//        }
//
//        @Override
//        public CreditCardAccount build() {
//            return new CreditCardAccount(getName(), getAccountNumber(), getBalance(), getIdentifiers(), getUniqueIdentifier(),
//                    getBankIdentifier(), getAvailableCredit(), getHolderName(), getTemporaryStorage());
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
//}
