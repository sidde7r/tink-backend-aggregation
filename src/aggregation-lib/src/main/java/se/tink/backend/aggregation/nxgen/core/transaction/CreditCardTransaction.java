package se.tink.backend.aggregation.nxgen.core.transaction;

import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.core.Amount;
import se.tink.backend.system.rpc.TransactionPayloadTypes;
import se.tink.backend.system.rpc.TransactionTypes;

public final class CreditCardTransaction extends Transaction {
    private final CreditCardAccount creditAccount;

    private CreditCardTransaction(Amount amount, Date date, String description, boolean pending, CreditCardAccount creditAccount) {
        super(amount, date, description, pending);
        this.creditAccount = creditAccount;
    }

    public Optional<CreditCardAccount> getCreditAccount() {
        return Optional.ofNullable(creditAccount);
    }

    @Override
    public TransactionTypes getType() {
        return TransactionTypes.CREDIT_CARD;
    }

    @Override
    public se.tink.backend.system.rpc.Transaction toSystemTransaction() {
        se.tink.backend.system.rpc.Transaction transaction = super.toSystemTransaction();

        getCreditAccount().ifPresent(creditAccount ->
                transaction.setPayload(TransactionPayloadTypes.SUB_ACCOUNT, creditAccount.getAccountNumber()));

        return transaction;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends Transaction.Builder {
        private CreditCardAccount creditAccount;

        private CreditCardAccount getCreditAccount() {
            return creditAccount;
        }

        public Builder setCreditAccount(CreditCardAccount creditAccount) {
            this.creditAccount = creditAccount;
            return this;
        }

        @Override
        public Builder setAmount(Amount amount) {
            return (Builder) super.setAmount(amount);
        }

        @Override
        public Builder setDate(Date date) {
            return (Builder) super.setDate(date);
        }

        @Override
        public Builder setDescription(String description) {
            return (Builder) super.setDescription(description);
        }

        @Override
        public Builder setPending(boolean pending) {
            return (Builder) super.setPending(pending);
        }

        @Override
        public Builder setRawDetails(Object rawDetails) {
            return (Builder) super.setRawDetails(rawDetails);
        }

        @Override
        public CreditCardTransaction build() {
            return new CreditCardTransaction(getAmount(), getDate(), getDescription(), isPending(), getCreditAccount());
        }
    }
}
