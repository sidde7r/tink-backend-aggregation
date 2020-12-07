package se.tink.backend.aggregation.nxgen.core.transaction;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.uuid.UUIDUtils;

public final class UpcomingTransaction extends AggregationTransaction {
    private final Transfer upcomingTransfer;

    protected UpcomingTransaction(
            ExactCurrencyAmount amount, Date date, String description, Transfer upcomingTransfer) {
        this(amount, date, description, upcomingTransfer, null, TransactionTypes.DEFAULT, null);
    }

    protected UpcomingTransaction(
            ExactCurrencyAmount amount,
            Date date,
            String description,
            Transfer upcomingTransfer,
            String rawDetails,
            TransactionTypes type,
            Map<TransactionPayloadTypes, String> payload) {
        super(amount, date, description, rawDetails, type, payload);
        this.upcomingTransfer = upcomingTransfer;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<Transfer> getUpcomingTransfer() {
        return Optional.ofNullable(upcomingTransfer);
    }

    @Override
    public Transaction toSystemTransaction(boolean multiCurrencyEnabled) {
        Transaction transaction = super.toSystemTransaction(multiCurrencyEnabled);

        transaction.setPending(true);

        getUpcomingTransfer()
                .ifPresent(
                        transfer -> {
                            transfer.setId(UUIDUtils.fromTinkUUID(transaction.getId()));
                            transaction.setPayload(
                                    TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER,
                                    SerializationUtils.serializeToString(transfer));
                            transaction.setPayload(
                                    TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER_ID,
                                    UUIDUtils.toTinkUUID(transfer.getId()));
                        });

        return transaction;
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

        @Override
        public UpcomingTransaction build() {
            return new UpcomingTransaction(
                    getExactAmount(),
                    getDate(),
                    getDescription(),
                    upcomingTransfer,
                    getRawDetails(),
                    getType(),
                    getPayload());
        }
    }
}
