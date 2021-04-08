package se.tink.backend.aggregation.nxgen.core.transaction;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class Transaction extends AggregationTransaction {
    private final boolean pending;
    private final String externalId;
    private final FieldsMigrations fieldsMigrations;

    protected Transaction(
            ExactCurrencyAmount amount,
            Date date,
            String description,
            boolean pending,
            Map<TransactionExternalSystemIdType, String> externalSystemIds,
            Boolean mutable,
            TransactionDates transactionDates,
            String proprietaryFinancialInstitutionType,
            String merchantName,
            String merchantCategoryCode,
            String transactionReference,
            String providerMarket) {
        this(
                amount,
                date,
                description,
                pending,
                null,
                null,
                TransactionTypes.DEFAULT,
                null,
                null,
                externalSystemIds,
                mutable,
                transactionDates,
                proprietaryFinancialInstitutionType,
                merchantName,
                merchantCategoryCode,
                transactionReference,
                providerMarket);
    }

    protected Transaction(
            ExactCurrencyAmount amount,
            Date date,
            String description,
            boolean pending,
            String rawDetails,
            String externalId,
            TransactionTypes type,
            FieldsMigrations fieldsMigrations,
            Map<TransactionPayloadTypes, String> payload,
            Map<TransactionExternalSystemIdType, String> externalSystemIds,
            Boolean mutable,
            TransactionDates transactionDates,
            String proprietaryFinancialInstitutionType,
            String merchantName,
            String merchantCategoryCode,
            String transactionReference,
            String providerMarket) {
        super(
                amount,
                date,
                description,
                rawDetails,
                type,
                payload,
                externalSystemIds,
                mutable,
                transactionDates,
                proprietaryFinancialInstitutionType,
                merchantName,
                merchantCategoryCode,
                transactionReference,
                providerMarket);
        this.pending = pending;
        this.externalId = externalId;
        this.fieldsMigrations = fieldsMigrations;
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

    public se.tink.backend.aggregation.agents.models.Transaction toSystemTransaction(
            boolean multiCurrencyEnabled) {
        se.tink.backend.aggregation.agents.models.Transaction transaction =
                super.toSystemTransaction(multiCurrencyEnabled);

        transaction.setPending(isPending());
        if (!Strings.isNullOrEmpty(getExternalId())) {
            transaction.setPayload(TransactionPayloadTypes.EXTERNAL_ID, getExternalId());
        }
        if (null != fieldsMigrations && fieldsMigrations.isNotEmpty()) {
            transaction.setPayload(
                    TransactionPayloadTypes.FIELD_MAPPER_MIGRATIONS, fieldsMigrations.toJSON());
        }

        return transaction;
    }

    public static class Builder extends AggregationTransaction.Builder {
        private boolean pending;
        private String externalId;
        private FieldsMigrations fieldsMigrations;

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

        public Builder setFieldsMigrations(FieldsMigrations fieldsMigrations) {
            this.fieldsMigrations = fieldsMigrations;
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
                    fieldsMigrations,
                    getPayload(),
                    getExternalSystemIds(),
                    getMutable(),
                    getTransactionDates(),
                    getProprietaryFinancialInstitutionType(),
                    getMerchantName(),
                    getMerchantCategoryCode(),
                    getTransactionReference(),
                    getProviderMarket());
        }
    }
}
