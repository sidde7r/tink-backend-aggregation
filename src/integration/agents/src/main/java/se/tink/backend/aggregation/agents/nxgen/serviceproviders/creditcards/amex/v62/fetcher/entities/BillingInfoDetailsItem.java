package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BillingInfoDetailsItem {

    @JsonProperty("billingIndex")
    private String billingIndex;

    @JsonProperty("statementDate")
    private String statementDate;

    @JsonProperty("label")
    private String label;

    @JsonProperty("title")
    private String title;

    public String getBillingIndex() {
        return billingIndex;
    }

    public String getStatementDate() {
        return statementDate;
    }

    public String getLabel() {
        return label;
    }

    public String getTitle() {
        return title;
    }
}
