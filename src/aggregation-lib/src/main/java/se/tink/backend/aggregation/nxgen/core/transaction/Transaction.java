package se.tink.backend.aggregation.nxgen.core.transaction;

import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.core.Amount;
import se.tink.backend.system.rpc.TransactionPayloadTypes;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Transaction extends AggregationTransaction {
    private final boolean pending;
    private final String rawDetails;
    private final String externalId;

    protected Transaction(Amount amount, Date date, String description, boolean pending) {
        this(amount, date, description, pending, null);
    }

    protected Transaction(Amount amount, Date date, String description, boolean pending, String rawDetails) {
        this(amount, date, description, pending, rawDetails, null);
    }

    protected Transaction(Amount amount, Date date, String description, boolean pending, String rawDetails,
            String externalId) {
        super(amount, date, description);
        this.pending = pending;
        this.rawDetails = rawDetails;
        this.externalId = externalId;
    }

    public boolean isPending() {
        return pending;
    }
    public String getRawDetails() {
        return rawDetails;
    }

    public String getExternalId() {
        return externalId;
    }

    public se.tink.backend.system.rpc.Transaction toSystemTransaction() {
        se.tink.backend.system.rpc.Transaction transaction = super.toSystemTransaction();

        transaction.setPending(isPending());
        if (!Strings.isNullOrEmpty(getRawDetails())) {
            transaction.setPayload(TransactionPayloadTypes.DETAILS, getRawDetails());
        }

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
        private String rawDetails;
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
            if (rawDetails != null) {
                if (rawDetails instanceof String) {
                    this.rawDetails = (String)rawDetails;
                } else {
                    this.rawDetails = SerializationUtils.serializeToString(rawDetails);
                }
            }
            return this;
        }

        String getRawDetails() {
            return rawDetails;
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
