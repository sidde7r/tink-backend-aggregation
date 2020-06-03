package se.tink.backend.aggregation.nxgen.core.transaction;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.user.rpc.User;

public class Transaction extends AggregationTransaction {
    private final boolean pending;
    private final String externalId;

    protected Transaction(
            ExactCurrencyAmount amount, Date date, String description, boolean pending) {
        this(amount, date, description, pending, null);
    }

    protected Transaction(
            ExactCurrencyAmount amount,
            Date date,
            String description,
            boolean pending,
            String rawDetails) {
        this(amount, date, description, pending, rawDetails, null, TransactionTypes.DEFAULT, null);
    }

    protected Transaction(
            ExactCurrencyAmount amount,
            Date date,
            String description,
            boolean pending,
            String rawDetails,
            String externalId,
            TransactionTypes type,
            Map<TransactionPayloadTypes, String> payload) {
        super(amount, date, description, rawDetails, type, payload);
        this.pending = pending;
        this.externalId = externalId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isPending() {
        return pending;
    }

    public String getExternalId() {
        return externalId;
    }

    public se.tink.backend.aggregation.agents.models.Transaction toSystemTransaction(User user) {
        se.tink.backend.aggregation.agents.models.Transaction transaction =
                super.toSystemTransaction(user);

        transaction.setPending(isPending());
        if (!Strings.isNullOrEmpty(getExternalId())) {
            transaction.setPayload(TransactionPayloadTypes.EXTERNAL_ID, getExternalId());
        }

        return transaction;
    }

    public static class Builder extends AggregationTransaction.Builder {
        private boolean pending;
        private String externalId;

        @Deprecated
        @Override
        public Builder setAmount(Amount amount) {
            return (Builder) super.setAmount(amount);
        }

        @Override
        public Builder setAmount(ExactCurrencyAmount amount) {
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

        boolean isPending() {
            return pending;
        }

        public Builder setPending(boolean pending) {
            this.pending = pending;
            return this;
        }

        @Override
        public Builder setRawDetails(Object rawDetails) {
            return (Builder) super.setRawDetails(rawDetails);
        }

        @Override
        public Builder setType(TransactionTypes type) {
            return (Builder) super.setType(type);
        }

        @Override
        public Builder setPayload(TransactionPayloadTypes key, String value) {
            return (Builder) super.setPayload(key, value);
        }

        String getExternalId() {
            return externalId;
        }

        @Override
        public Transaction build() {
            return new Transaction(
                    getExactAmount(),
                    getDate(),
                    getDescription(),
                    isPending(),
                    getRawDetails(),
                    getExternalId(),
                    getType(),
                    getPayload());
        }
    }
}
