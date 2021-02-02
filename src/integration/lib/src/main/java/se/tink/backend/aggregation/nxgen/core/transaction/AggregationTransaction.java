package se.tink.backend.aggregation.nxgen.core.transaction;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.models.TransactionMutability;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public abstract class AggregationTransaction {
    private static final TypeReference<HashMap<String, String>> HASH_MAP_REFERENCE =
            new TypeReference<HashMap<String, String>>() {};
    private final ExactCurrencyAmount amount;
    private final String description;
    private final Date date;
    private final String rawDetails;
    private final TransactionTypes type;
    private final Map<TransactionPayloadTypes, String> payload;
    private final Map<TransactionExternalSystemIdType, String> externalSystemIds;
    private final Boolean mutable;
    private final List<TransactionDate> transactionDates;
    private final String proprietaryFinancialInstitutionType;
    private final String merchantName;
    private final String merchantCategoryCode;
    private final String transactionReference;
    private final String providerMarket;

    protected AggregationTransaction(
            ExactCurrencyAmount amount,
            Date date,
            String description,
            String rawDetails,
            TransactionTypes type,
            Map<TransactionPayloadTypes, String> payload,
            Map<TransactionExternalSystemIdType, String> externalSystemIds,
            Boolean mutable,
            List<TransactionDate> transactionDates,
            String proprietaryFinancialInstitutionType,
            String merchantName,
            String merchantCategoryCode,
            String transactionReference,
            String providerMarket) {
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.rawDetails = rawDetails;
        this.type = type;
        this.payload = payload;
        this.externalSystemIds = externalSystemIds;
        this.mutable = mutable;
        this.transactionDates = transactionDates;
        this.proprietaryFinancialInstitutionType = proprietaryFinancialInstitutionType;
        this.merchantName = merchantName;
        this.merchantCategoryCode = merchantCategoryCode;
        this.transactionReference = transactionReference;
        this.providerMarket = providerMarket;
    }

    public ExactCurrencyAmount getExactAmount() {
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
        return type;
    }

    public Map<TransactionPayloadTypes, String> getPayload() {
        return payload;
    }

    public ExactCurrencyAmount getAmount() {
        return amount;
    }

    public Map<TransactionExternalSystemIdType, String> getExternalSystemIds() {
        return externalSystemIds;
    }

    public Boolean getMutable() {
        return mutable;
    }

    public List<TransactionDate> getTransactionDates() {
        return transactionDates;
    }

    public String getMerchantCategoryCode() {
        return merchantCategoryCode;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getProprietaryFinancialInstitutionType() {
        return proprietaryFinancialInstitutionType;
    }

    public String getProviderMarket() {
        return providerMarket;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public Transaction toSystemTransaction(boolean multiCurrencyEnabled) {

        Transaction transaction = new Transaction();

        transaction.setAmount(getExactAmount().getDoubleValue());
        transaction.setDescription(getDescription());
        transaction.setDate(getDate());
        transaction.setType(getType());
        transaction.setProprietaryFinancialInstitutionType(
                getProprietaryFinancialInstitutionType());
        transaction.setMerchantName(getMerchantName());
        transaction.setMerchantCategoryCode(getMerchantCategoryCode());
        transaction.setProviderMarket(getProviderMarket());
        transaction.setTransactionReference(getTransactionReference());

        if (!Strings.isNullOrEmpty(getRawDetails()) || multiCurrencyEnabled) {
            transaction.setPayload(
                    TransactionPayloadTypes.DETAILS,
                    addCurrencyIfEligible(multiCurrencyEnabled, getRawDetails()));
        }
        if (payload != null) {
            payload.forEach((key, value) -> transaction.setPayload(key, value));
        }

        transaction.setMutability(TransactionMutability.valueOf(getMutable()));
        transaction.setExternalSystemIds(
                getExternalSystemIds() != null
                        ? getExternalSystemIds().entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                        : null);
        transaction.setTransactionDates(
                getTransactionDates() != null
                        ? getTransactionDates().stream()
                                .map(TransactionDate::toSystemModel)
                                .collect(Collectors.toList())
                        : null);

        return transaction;
    }

    private String addCurrencyIfEligible(boolean multiCurrencyEnabled, String rawDetails) {
        if (!multiCurrencyEnabled) {
            return rawDetails;
        }

        // This solution will not work for agents adding custom raw details that are not
        // HashMap<String, String>, which
        // a few belgian agents does.
        HashMap<String, String> map =
                !Strings.isNullOrEmpty(getRawDetails())
                        ? SerializationUtils.deserializeFromString(rawDetails, HASH_MAP_REFERENCE)
                        : Maps.newHashMap();

        if (map == null) {
            return rawDetails;
        }
        map.put("currency", amount.getCurrencyCode());
        return SerializationUtils.serializeToString(map);
    }

    public abstract static class Builder {
        private ExactCurrencyAmount amount;
        private String description;
        private Date date;
        private String rawDetails;
        private TransactionTypes type = TransactionTypes.DEFAULT;
        private Map<TransactionPayloadTypes, String> payload = Maps.newHashMap();
        private Map<TransactionExternalSystemIdType, String> externalSystemIds = Maps.newHashMap();
        private Boolean mutable;
        private List<TransactionDate> transactionDates = new ArrayList<>();
        private String proprietaryFinancialInstitutionType;
        private String merchantName;
        private String merchantCategoryCode;
        private String transactionReference;
        private String providerMarket;

        @Deprecated
        public Builder setAmount(Amount amount) {
            this.amount = ExactCurrencyAmount.of(amount.toBigDecimal(), amount.getCurrency());
            return this;
        }

        public Builder setAmount(ExactCurrencyAmount amount) {
            this.amount = amount;
            return this;
        }

        ExactCurrencyAmount getExactAmount() {
            Preconditions.checkNotNull(amount);
            return amount;
        }

        String getDescription() {
            return description;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setDateTime(ZonedDateTime dateTime) {
            return setDate(dateTime.toLocalDate());
        }

        public Builder setDate(CharSequence date, DateTimeFormatter formatter) {
            return setDate(DateUtils.toJavaUtilDate(date, formatter));
        }

        Date getDate() {
            return date != null ? DateUtils.flattenTime(date) : null;
        }

        public Builder setDate(Date date) {
            this.date = date;
            return this;
        }

        public Builder setDate(LocalDate date) {
            return setDate(DateUtils.toJavaUtilDate(date));
        }

        String getRawDetails() {
            return rawDetails;
        }

        public Builder setRawDetails(Object rawDetails) {
            if (rawDetails != null) {
                if (rawDetails instanceof String) {
                    this.rawDetails = (String) rawDetails;
                } else {
                    this.rawDetails = SerializationUtils.serializeToString(rawDetails);
                }
            }
            return this;
        }

        public TransactionTypes getType() {
            return type;
        }

        public Builder setType(TransactionTypes type) {
            this.type = type;
            return this;
        }

        public Map<TransactionPayloadTypes, String> getPayload() {
            return payload;
        }

        public Builder setPayload(TransactionPayloadTypes key, String value) {
            payload.put(key, value);
            return this;
        }

        public Builder addExternalSystemIds(TransactionExternalSystemIdType key, String value) {
            this.externalSystemIds.put(key, value);
            return this;
        }

        public Builder setExternalSystemIds(
                Map<TransactionExternalSystemIdType, String> externalSystemIds) {
            Preconditions.checkNotNull(externalSystemIds, "ExternalSystemIds must not be null.");
            this.externalSystemIds.putAll(externalSystemIds);
            return this;
        }

        public Map<TransactionExternalSystemIdType, String> getExternalSystemIds() {
            return externalSystemIds;
        }

        public Builder setMutable(Boolean mutable) {
            this.mutable = mutable;
            return this;
        }

        public Boolean getMutable() {
            return mutable;
        }

        public Builder addTransactionDates(List<TransactionDate> transactionDates) {
            Preconditions.checkNotNull(transactionDates, "TransactionDates must not be null.");
            this.transactionDates.addAll(transactionDates);
            return this;
        }

        public Builder addTransactionDates(TransactionDate... transactionDates) {
            Preconditions.checkNotNull(transactionDates, "TransactionDates must not be null.");
            return this.addTransactionDates(Arrays.asList(transactionDates));
        }

        public List<TransactionDate> getTransactionDates() {
            return transactionDates;
        }

        public String getProprietaryFinancialInstitutionType() {
            return proprietaryFinancialInstitutionType;
        }

        public Builder setProprietaryFinancialInstitutionType(
                String proprietaryFinancialInstitutionType) {
            this.proprietaryFinancialInstitutionType = proprietaryFinancialInstitutionType;
            return this;
        }

        public String getMerchantName() {
            return merchantName;
        }

        public Builder setMerchantName(String merchantName) {
            this.merchantName = merchantName;
            return this;
        }

        public String getMerchantCategoryCode() {
            return merchantCategoryCode;
        }

        public Builder setMerchantCategoryCode(String merchantCategoryCode) {
            this.merchantCategoryCode = merchantCategoryCode;
            return this;
        }

        public String getTransactionReference() {
            return transactionReference;
        }

        public Builder setTransactionReference(String transactionReference) {
            this.transactionReference = transactionReference;
            return this;
        }

        public String getProviderMarket() {
            return providerMarket;
        }

        public Builder setProviderMarket(String providerMarket) {
            this.providerMarket = providerMarket;
            return this;
        }

        public abstract AggregationTransaction build();
    }
}
