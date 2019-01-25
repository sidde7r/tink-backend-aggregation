package se.tink.backend.aggregation.nxgen.core.transaction;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.libraries.amount.Amount;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.agents.rpc.User;

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
    public se.tink.backend.aggregation.agents.models.Transaction toSystemTransaction(User user) {
        se.tink.backend.aggregation.agents.models.Transaction transaction = super.toSystemTransaction(user);

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
        public Builder setDate(LocalDate date) {
            return (Builder) super.setDate(date);
        }

        @Override
        public Builder setDateTime(ZonedDateTime dateTime) {
            return (Builder) super.setDate(dateTime.toLocalDate());
        }

        @Override
        public Builder setDate(CharSequence date, DateTimeFormatter formatter) {
            return (Builder) super.setDate(date, formatter);
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
