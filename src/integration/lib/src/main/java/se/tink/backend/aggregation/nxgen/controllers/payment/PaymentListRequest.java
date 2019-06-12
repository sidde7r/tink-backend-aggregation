package se.tink.backend.aggregation.nxgen.controllers.payment;

import java.util.ArrayList;
import java.util.List;

public class PaymentListRequest {
    private List<PaymentRequest> paymentRequestList;

    public PaymentListRequest(List<PaymentRequest> paymentRequestList) {
        this.paymentRequestList = paymentRequestList;
    }

    public PaymentListRequest(PaymentRequest paymentRequest) {
        this.paymentRequestList = new ArrayList<>();
        this.getPaymentRequestList().add(paymentRequest);
    }

    public List<PaymentRequest> getPaymentRequestList() {
        return paymentRequestList;
    }
}
