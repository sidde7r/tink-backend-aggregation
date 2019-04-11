package se.tink.backend.aggregation.nxgen.controllers.payment;

import java.util.ArrayList;
import java.util.List;

public class PaymentListResponse {
    private List<PaymentResponse> paymentResponseList;

    public PaymentListResponse(List<PaymentResponse> paymentResponseList) {
        this.paymentResponseList = paymentResponseList;
    }

    public PaymentListResponse(PaymentResponse paymentResponse) {
        this.paymentResponseList = new ArrayList<>();
        this.getPaymentResponseList().add(paymentResponse);
    }


    public List<PaymentResponse> getPaymentResponseList() {
        return paymentResponseList;
    }
}
