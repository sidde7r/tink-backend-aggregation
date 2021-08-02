package se.tink.backend.aggregation.nxgen.controllers.payment;

import lombok.Getter;
import se.tink.libraries.payment.rpc.Payment;

@Getter
public class PaymentMultiStepResponse extends PaymentResponse {
    private String step;

    public PaymentMultiStepResponse(Payment payment, String step) {
        super(payment);
        this.step = step;
    }

    public PaymentMultiStepResponse(PaymentMultiStepRequest paymentMultiStepRequest, String step) {
        super(paymentMultiStepRequest.getPayment(), paymentMultiStepRequest.getStorage());
        this.step = step;
    }

    public PaymentMultiStepResponse(PaymentResponse paymentResponse, String step) {
        super(paymentResponse.getPayment(), paymentResponse.getStorage());
        this.step = step;
    }
}
