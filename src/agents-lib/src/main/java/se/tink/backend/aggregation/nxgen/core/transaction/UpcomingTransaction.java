package se.tink.backend.aggregation.nxgen.core.transaction;

import java.util.Date;
import java.util.Optional;
import se.tink.backend.core.Amount;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.system.rpc.TransactionPayloadTypes;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public final class UpcomingTransaction extends AggregationTransaction {
    private final Transfer upcomingTransfer;

    protected UpcomingTransaction(Amount amount, Date date, String description, Transfer upcomingTransfer) {
        this(amount, date, description, upcomingTransfer, null);
    }

    protected UpcomingTransaction(Amount amount, Date date, String description, Transfer upcomingTransfer, String rawDetails) {
        super(amount, date, description, rawDetails);
        this.upcomingTransfer = upcomingTransfer;
    }

    public Optional<Transfer> getUpcomingTransfer() {
        return Optional.ofNullable(upcomingTransfer);
    }

    @Override
    public se.tink.backend.system.rpc.Transaction toSystemTransaction() {
        se.tink.backend.system.rpc.Transaction transaction = super.toSystemTransaction();

        transaction.setPending(true);

        getUpcomingTransfer().ifPresent(transfer -> {
            transfer.setId(UUIDUtils.fromTinkUUID(transaction.getId()));
            transaction.setPayload(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER,
                    SerializationUtils.serializeToString(transfer));
            transaction.setPayload(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER_ID,
                    UUIDUtils.toTinkUUID(transfer.getId()));
        });

        return transaction;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends AggregationTransaction.Builder {
        private Transfer upcomingTransfer;

        public Transfer getUpcomingTransfer() {
            return upcomingTransfer;
        }

        public Builder setUpcomingTransfer(Transfer upcomingTransfer) {
            this.upcomingTransfer = upcomingTransfer;
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

        public Builder setRawDetails(Object rawDetails) {
            return (Builder) super.setRawDetails(rawDetails);
        }

        @Override
        public UpcomingTransaction build() {
            return new UpcomingTransaction(getAmount(), getDate(), getDescription(), upcomingTransfer, getRawDetails());
        }
    }
}
