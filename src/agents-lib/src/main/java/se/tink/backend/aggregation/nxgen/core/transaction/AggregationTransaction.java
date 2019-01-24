package se.tink.backend.aggregation.nxgen.core.transaction;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.rpc.User;
import se.tink.backend.core.Amount;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public abstract class AggregationTransaction {
    private static final TypeReference<HashMap<String, String>> HASH_MAP_REFERENCE = new TypeReference<HashMap<String, String>>() { };
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

    public Transaction toSystemTransaction(User user) {
        Transaction transaction = new Transaction();

        transaction.setAmount(getAmount().getValue());
        transaction.setDescription(getDescription());
        transaction.setDate(getDate());
        transaction.setType(getType());

        boolean multiCurrencyEnabled = FeatureFlags.FeatureFlagGroup.MULTI_CURRENCY_FOR_POCS.isFlagInGroup(user.getFlags());
        if (!Strings.isNullOrEmpty(getRawDetails()) || multiCurrencyEnabled) {
            transaction.setPayload(TransactionPayloadTypes.DETAILS, addCurrencyIfEligible(multiCurrencyEnabled, getRawDetails()));
        }

        return transaction;
    }

    private String addCurrencyIfEligible(boolean multiCurrencyEnabled, String rawDetails) {
        if (!multiCurrencyEnabled) {
            return rawDetails;
        }

        // This solution will not work for agents adding custom raw details that are not HashMap<String, String>, which
        // a few belgian agents does.
        HashMap<String, String> map = !Strings.isNullOrEmpty(getRawDetails()) ?
                SerializationUtils.deserializeFromString(rawDetails, HASH_MAP_REFERENCE) :
                Maps.newHashMap();

        if (map == null) {
            return rawDetails;
        }
        map.put("currency", amount.getCurrency());
        return SerializationUtils.serializeToString(map);
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

        public Builder setDateTime(ZonedDateTime dateTime) {
            return setDate(dateTime.toLocalDate());
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
