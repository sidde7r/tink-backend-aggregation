package se.tink.backend.aggregation.nxgen.controllers.payment;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.agents.rpc.Field;
import se.tink.libraries.payment.rpc.Payment;

public class PaymentMultiStepResponse extends PaymentResponse {
    private String step;
    private List<Field> fields;

    public PaymentMultiStepResponse(Payment payment, String step, List<Field> fields) {
        super(payment);
        this.step = step;
        this.fields = fields;
    }

    public PaymentMultiStepResponse(
            PaymentMultiStepRequest paymentMultiStepRequest, String step, ArrayList<Field> fields) {
        super(paymentMultiStepRequest.getPayment(), paymentMultiStepRequest.getStorage());
        this.step = step;
        this.fields = fields;
    }

    public PaymentMultiStepResponse(
            PaymentResponse paymentResponse, String step, List<Field> fields) {
        super(paymentResponse.getPayment(), paymentResponse.getStorage());
        this.step = step;
        this.fields = fields;
    }

    public String getStep() {
        return step;
    }

    public List<Field> getFields() {
        return fields;
    }
}
