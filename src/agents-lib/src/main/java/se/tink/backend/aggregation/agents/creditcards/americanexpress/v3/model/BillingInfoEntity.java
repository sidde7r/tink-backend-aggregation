package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

import java.util.List;

public class BillingInfoEntity {
    private String message;
    private List<BillingInfoDetailsEntity> billingInfoDetails;

    public List<BillingInfoDetailsEntity> getBillingInfoDetails() {
        return billingInfoDetails;
    }

    public void setBillingInfoDetails(List<BillingInfoDetailsEntity> billingInfoDetails) {
        this.billingInfoDetails = billingInfoDetails;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
