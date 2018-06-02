package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentsListResponse {

    private List<PaymentEntity> payments;
    private boolean signingIsRequired;
    private double sumOfUnsignedItems;

    public List<PaymentEntity> getPayments() {
        return payments;
    }

    public double getSumOfUnsignedItems() {
        return sumOfUnsignedItems;
    }

    public boolean isSigningIsRequired() {
        return signingIsRequired;
    }

    public void setPayments(List<PaymentEntity> payments) {
        this.payments = payments;
    }

    public void setSigningIsRequired(boolean signingIsRequired) {
        this.signingIsRequired = signingIsRequired;
    }

    public void setSumOfUnsignedItems(double sumOfUnsignedItems) {
        this.sumOfUnsignedItems = sumOfUnsignedItems;
    }
}
