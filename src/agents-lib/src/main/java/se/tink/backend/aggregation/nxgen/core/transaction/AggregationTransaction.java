package se.tink.backend.aggregation.nxgen.core.transaction;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import se.tink.backend.core.Amount;
import se.tink.backend.system.rpc.TransactionPayloadTypes;
import se.tink.backend.system.rpc.TransactionTypes;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public abstract class AggregationTransaction {
    private final Amount amount;
    private final String description;
    private final Date date;
    private final String rawDetails;

    protected AggregationTransaction(Amount amount, Date date, String description, String rawDetails) {
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.rawDetails = rawDetails;
    }

    public Amount getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public Date getDate() {
        return date;
    }

    public String getRawDetails() {
        return rawDetails;
    }

    public TransactionTypes getType() {
        return TransactionTypes.DEFAULT;
    }

    public se.tink.backend.system.rpc.Transaction toSystemTransaction() {
        se.tink.backend.system.rpc.Transaction transaction = new se.tink.backend.system.rpc.Transaction();

        transaction.setAmount(getAmount().getValue());
        transaction.setDescription(getDescription());
        transaction.setDate(getDate());
        transaction.setType(getType());

        if (!Strings.isNullOrEmpty(getRawDetails())) {
            transaction.setPayload(TransactionPayloadTypes.DETAILS, getRawDetails());
        }

        return transaction;
    }

    public static abstract class Builder {
        private Amount amount;
        private String description;
        private Date date;
        private String rawDetails;

        public Builder setAmount(Amount amount) {
            this.amount = amount;
            return this;
        }

        Amount getAmount() {
            return Preconditions.checkNotNull(amount);
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        String getDescription() {
            return description;
        }

        public Builder setDate(Date date) {
            this.date = date;
            return this;
        }

        public Builder setDate(LocalDate date) {
            return setDate(DateUtils.toJavaUtilDate(date));
        }

        public Builder setDate(CharSequence date, DateTimeFormatter formatter) {
            return setDate(DateUtils.toJavaUtilDate(date, formatter));
        }

        Date getDate() {
            return date != null ? DateUtils.flattenTime(date) : null;
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

        public abstract AggregationTransaction build();
    }
}
