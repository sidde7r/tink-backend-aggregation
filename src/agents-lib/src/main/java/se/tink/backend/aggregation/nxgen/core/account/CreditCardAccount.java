package se.tink.backend.aggregation.nxgen.core.account;

import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;

public class CreditCardAccount extends Account {
    private final Amount availableCredit;

    private CreditCardAccount(Builder<CreditCardAccount, DefaultCreditCardBuilder> builder) {
        super(builder);
        this.availableCredit = builder.getAvailableCredit();
    }

    public static Builder<?, ?> builder(String uniqueIdentifier) {
        return new DefaultCreditCardBuilder(uniqueIdentifier);
    }

    public static Builder<?, ?> builder(String uniqueIdentifier, Amount balance, Amount availableCredit) {
        return builder(uniqueIdentifier)
                .setBalance(balance)
                .setAvailableCredit(availableCredit);
    }

    public Amount getAvailableCredit() {
        return new Amount(this.availableCredit.getCurrency(), this.availableCredit.getValue());
    }

    @Override
    public AccountTypes getType() {
        return AccountTypes.CREDIT_CARD;
    }

    @Override
    public se.tink.backend.aggregation.rpc.Account toSystemAccount() {
        se.tink.backend.aggregation.rpc.Account account = super.toSystemAccount();

        account.setAvailableCredit(this.availableCredit.getValue());

        return account;
    }

    public abstract static class Builder<
            A extends CreditCardAccount, T extends CreditCardAccount.Builder<A, T>>
            extends Account.Builder<A, T> {

        private Amount availableCredit;

        public Builder(String uniqueIdentifier) {
            super(uniqueIdentifier);
        }

        public Amount getAvailableCredit() {
            return Amount.createFromAmount(this.availableCredit).orElseThrow(NullPointerException::new);
        }

        public Builder<A, T> setAvailableCredit(Amount availableCredit) {
            this.availableCredit = availableCredit;
            return self();
        }
    }

    private static class DefaultCreditCardBuilder
            extends CreditCardAccount.Builder<CreditCardAccount, DefaultCreditCardBuilder> {

        public DefaultCreditCardBuilder(String uniqueIdentifier) {
            super(uniqueIdentifier);
        }

        @Override
        protected DefaultCreditCardBuilder self() {
            return this;
        }

        @Override
        public CreditCardAccount build() {
            return new CreditCardAccount(self());
        }
    }
}
