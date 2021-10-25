package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date transactionTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date settlementTime;

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
                        .setDate(transactionTime)
                        .setDescription(reference)
                        .setPending(isPending())
                        .setProprietaryFinancialInstitutionType(source);

        if (isMerchantCounterPartyType()) {
            builder.setMerchantName(counterPartyName);
        }
        return (Transaction) builder.build();
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
}
