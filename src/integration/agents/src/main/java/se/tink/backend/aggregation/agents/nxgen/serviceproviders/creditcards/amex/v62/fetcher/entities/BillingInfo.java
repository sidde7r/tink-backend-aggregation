package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BillingInfo {

    @JsonProperty("billingInfoDetails")
    private List<BillingInfoDetailsItem> billingInfoDetails;

    @JsonProperty("message")
    private String message;

    public List<BillingInfoDetailsItem> getBillingInfoDetails() {
        return billingInfoDetails;
    }

    public String getMessage() {
        return message;
    }
}
