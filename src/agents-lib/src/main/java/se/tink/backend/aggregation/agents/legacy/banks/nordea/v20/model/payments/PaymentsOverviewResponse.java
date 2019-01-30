package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)

public class PaymentsOverviewResponse {

    @JsonProperty("getPaymentOverviewOut")
    private PaymentsOverviewOut paymentsOverviewOut;

    public PaymentsOverviewOut getPaymentsOverviewOut() {
        return paymentsOverviewOut;
    }

    public void setPaymentsOverviewOut(
            PaymentsOverviewOut paymentsOverviewOut) {
        this.paymentsOverviewOut = paymentsOverviewOut;
    }

    public Integer getNumberOfUnconfirmedPayments() {
        return Integer.parseInt(paymentsOverviewOut.getPaymentCount());
    }
}
