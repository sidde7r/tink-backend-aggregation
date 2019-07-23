package se.tink.backend.aggregation.nxgen.controllers.payment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentListResponse {
    private List<PaymentResponse> paymentResponseList;

    public PaymentListResponse(List<PaymentResponse> paymentResponseList) {
        this.paymentResponseList = paymentResponseList;
    }

    public PaymentListResponse(PaymentResponse paymentResponse) {
        this.paymentResponseList = new ArrayList<>();
        this.getPaymentResponseList().add(paymentResponse);
    }

    public static PaymentListResponse of(List<PaymentRequest> paymentRequests) {
        return new PaymentListResponse(
                paymentRequests.stream().map(PaymentResponse::of).collect(Collectors.toList()));
    }

    public List<PaymentResponse> getPaymentResponseList() {
        return paymentResponseList;
    }
}
