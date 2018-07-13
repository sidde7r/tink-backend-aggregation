package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BillingInfoEntity {
    private String message;
    private List<BillingInfoDetailsEntity> billingInfoDetails;

    public String getMessage() {
        return message;
    }

    public List<BillingInfoDetailsEntity> getBillingInfoDetails() {
        return billingInfoDetails;
    }
}
