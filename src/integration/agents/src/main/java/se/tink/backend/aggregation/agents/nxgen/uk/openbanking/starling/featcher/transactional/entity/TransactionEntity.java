package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.TransactionDateMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkObInstantDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;

@JsonObject
public class TransactionEntity {

    private static final List<String> relevantStatusList =
            new ArrayList<>(Arrays.asList("settled", "pending", "retrying"));

    private static final List<String> pendingStatusList =
            new ArrayList<>(Arrays.asList("pending", "retrying"));

    private static final String OUT_DIRECTION = "OUT";

    private static final String MERCHANT_COUNTER_PARTY_TYPE = "MERCHANT";

    private String feedItemUid;
    private String categoryUid;
    private AmountEntity amount;
    private AmountEntity sourceAmount;

    private String direction;

    @JsonDeserialize(using = UkObInstantDeserializer.class)
    private Instant updatedAt;

    @JsonDeserialize(using = UkObInstantDeserializer.class)
    private Instant transactionTime;

    @JsonDeserialize(using = UkObInstantDeserializer.class)
    private Instant settlementTime;

    private String source;
    private String sourceSubType;
    private String status;
    private String reference;
    private String counterPartyType;
    private String counterPartyName;
    private String country;
    private String userNote;

    public Transaction toTinkTransaction() {

        ExactCurrencyAmount transactionAmount = amount.toExactCurrencyAmount();

        if (direction.equals(OUT_DIRECTION)) {
            transactionAmount = transactionAmount.negate();
        }

        Builder builder =
                Transaction.builder()
                        .setAmount(transactionAmount)
                        .setDescription(reference)
                        .setPending(isPending())
                        .setProprietaryFinancialInstitutionType(source)
                        .setProviderMarket(String.valueOf(MarketCode.UK));

        if (hasFeedItemUid()) {
            builder.addExternalSystemIds(
                    TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID, feedItemUid);
        }

        if (hasSettlementTime()) {
            builder.setDate(getDateOfTransaction(settlementTime));
            builder.setTransactionDates(getTransactionDates(settlementTime));
        } else {
            builder.setDate(getDateOfTransaction(transactionTime));
            builder.setTransactionDates(getTransactionDates(transactionTime));
        }

        if (isMerchantCounterPartyType()) {
            builder.setMerchantName(counterPartyName);
        }
        return (Transaction) builder.build();
    }

    @JsonIgnore
    private Date getDateOfTransaction(Instant instant) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return simpleDateFormat.parse(instant.toString());
        } catch (ParseException e) {
            return Date.from(instant);
        }
    }

    @JsonIgnore
    private TransactionDates getTransactionDates(Instant date) {
        return TransactionDates.builder()
                .setBookingDate(TransactionDateMapper.prepareTransactionDate(date))
                .build();
    }

    public String getStatus() {
        return status;
    }

    @JsonIgnore
    public boolean isRelevant() {
        return relevantStatusList.contains(getStatus().toLowerCase());
    }

    @JsonIgnore
    private boolean isPending() {
        return pendingStatusList.contains(getStatus().toLowerCase());
    }

    @JsonIgnore
    private boolean isMerchantCounterPartyType() {
        return MERCHANT_COUNTER_PARTY_TYPE.equals(counterPartyType);
    }

    @JsonIgnore
    private boolean hasSettlementTime() {
        return settlementTime != null;
    }

    @JsonIgnore
    private boolean hasFeedItemUid() {
        return feedItemUid != null;
    }
}
