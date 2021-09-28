package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.TransactionDirections;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {

    private static final List<String> relevantStatusList =
            new ArrayList<>(Arrays.asList("settled", "pending", "retrying"));

    private static final List<String> pendingStatusList =
            new ArrayList<>(Arrays.asList("pending", "retrying"));

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
    private String country;
    private String userNote;

    public Transaction toTinkTransaction() {
        ExactCurrencyAmount transactionAmount = amount.toExactCurrencyAmount();

        if (direction.equals(TransactionDirections.OUT)) {
            transactionAmount = transactionAmount.negate();
        }

        return (Transaction)
                Transaction.builder()
                        .setAmount(transactionAmount)
                        .setDate(transactionTime)
                        .setDescription(reference)
                        .setPending(isPending())
                        .setProprietaryFinancialInstitutionType(source)
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
}
