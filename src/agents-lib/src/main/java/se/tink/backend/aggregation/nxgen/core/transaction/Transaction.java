package se.tink.backend.aggregation.nxgen.core.transaction;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import se.tink.libraries.amount.Amount;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.agents.rpc.User;

public class Transaction extends AggregationTransaction {
    private final boolean pending;
    private final String externalId;

    protected Transaction(Amount amount, Date date, String description, boolean pending) {
        this(amount, date, description, pending, null);
    }

    protected Transaction(Amount amount, Date date, String description, boolean pending, String rawDetails) {
        this(amount, date, description, pending, rawDetails, null);
    }

    protected Transaction(Amount amount, Date date, String description, boolean pending, String rawDetails,
            String externalId) {
        super(amount, date, description, rawDetails);
        this.pending = pending;
        this.externalId = externalId;
    }

    public boolean isPending() {
        return pending;
    }

    public String getExternalId() {
        return externalId;
    }

    public se.tink.backend.aggregation.agents.models.Transaction toSystemTransaction(User user) {
        se.tink.backend.aggregation.agents.models.Transaction transaction = super.toSystemTransaction(user);

        transaction.setPending(isPending());
        if (!Strings.isNullOrEmpty(getExternalId())) {
            transaction.setPayload(TransactionPayloadTypes.EXTERNAL_ID, getExternalId());
        }

        return transaction;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AggregationTransaction.Builder {
        private boolean pending;
        private String externalId;

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

        public Builder setPending(boolean pending) {
            this.pending = pending;
            return this;
        }

        boolean isPending() {
            return pending;
        }

        public Builder setRawDetails(Object rawDetails) {
            return (Builder) super.setRawDetails(rawDetails);
        }

        public Builder setExternalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        String getExternalId() {
            return externalId;
        }

        @Override
        public Transaction build() {
            return new Transaction(getAmount(), getDate(), getDescription(), isPending(), getRawDetails(),
                    getExternalId());
        }
    }
}
