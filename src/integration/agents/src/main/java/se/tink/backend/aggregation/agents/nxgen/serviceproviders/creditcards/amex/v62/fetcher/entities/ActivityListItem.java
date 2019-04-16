package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ActivityListItem {

    @JsonProperty("messageType")
    private String messageType;

    @JsonProperty("transactionList")
    private List<TransactionListItem> transactionList;

    @JsonProperty("flexEnrolled")
    private boolean flexEnrolled;

    @JsonProperty("billingIndex")
    private String billingIndex;

    @JsonProperty("message")
    private String message;

    public String getMessageType() {
        return messageType;
    }

    public List<TransactionListItem> getTransactionList() {
        return transactionList;
    }

    public boolean isFlexEnrolled() {
        return flexEnrolled;
    }

    public String getBillingIndex() {
        return billingIndex;
    }

    public String getMessage() {
        return message;
    }
}
