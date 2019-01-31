package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentListResponse {

    private PaymentsOut getPaymentListOut;

    public PaymentsOut getGetPaymentListOut() {
        return getPaymentListOut;
    }

    public void setGetPaymentListOut(PaymentsOut getPaymentListOut) {
        this.getPaymentListOut = getPaymentListOut;
    }

    @JsonIgnore
    public List<PaymentEntity> getPayments(Payment.StatusCode statusCode) {
        if (getPaymentListOut == null) {
            return Lists.newArrayList();
        }

        return getPaymentListOut.getPayments(statusCode);
    }
}
