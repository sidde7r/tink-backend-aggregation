package se.tink.backend.aggregation.nxgen.core.transaction;

import com.google.common.base.Preconditions;
import java.util.Date;
import se.tink.backend.core.Amount;
import se.tink.backend.system.rpc.TransactionTypes;
import se.tink.libraries.date.DateUtils;

public abstract class AggregationTransaction {
    private final Amount amount;
    private final String description;
    private final Date date;

    protected AggregationTransaction(Amount amount, Date date, String description) {
        this.amount = amount;
        this.date = date;
        this.description = description;
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

    public TransactionTypes getType() {
        return TransactionTypes.DEFAULT;
    }

    public se.tink.backend.system.rpc.Transaction toSystemTransaction() {
        se.tink.backend.system.rpc.Transaction transaction = new se.tink.backend.system.rpc.Transaction();

        transaction.setAmount(getAmount().getValue());
        transaction.setDescription(getDescription());
        transaction.setDate(getDate());
        transaction.setType(getType());

        return transaction;
    }

    public static abstract class Builder {
        private Amount amount;
        private String description;
        private Date date;

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

        Date getDate() {
            return date != null ? DateUtils.flattenTime(date) : null;
        }

        public abstract AggregationTransaction build();
    }
}
