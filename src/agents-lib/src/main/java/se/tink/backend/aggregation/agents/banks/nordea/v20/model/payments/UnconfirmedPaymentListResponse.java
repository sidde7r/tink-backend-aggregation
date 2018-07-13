package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UnconfirmedPaymentListResponse {

    private PaymentsOut getUnconfirmedPaymentsOut;

    public PaymentsOut getGetUnconfirmedPaymentsOut() {
        return getUnconfirmedPaymentsOut;
    }

    public void setGetUnconfirmedPaymentsOut(PaymentsOut getUnconfirmedPaymentsOut) {
        this.getUnconfirmedPaymentsOut = getUnconfirmedPaymentsOut;
    }

    @JsonIgnore
    public List<PaymentEntity> getPayments(Payment.StatusCode statusCode) {
        if (getUnconfirmedPaymentsOut == null) {
            return Lists.newArrayList();
        }

        return getUnconfirmedPaymentsOut.getPayments(statusCode);
    }
}
