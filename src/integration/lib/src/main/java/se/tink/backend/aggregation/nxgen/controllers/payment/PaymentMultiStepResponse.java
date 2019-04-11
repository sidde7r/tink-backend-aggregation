package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.agents.rpc.Field;
import se.tink.libraries.payment.rpc.Payment;

import java.util.List;

public class PaymentMultiStepResponse extends PaymentResponse {
    private String step;
    private List<Field> fields;

    public PaymentMultiStepResponse(Payment payment, String step, List<Field> fields) {
        super(payment);
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
