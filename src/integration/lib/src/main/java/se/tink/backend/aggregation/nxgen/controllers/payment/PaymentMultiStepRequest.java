package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.agents.rpc.Field;
import se.tink.libraries.payment.rpc.Payment;

import java.util.List;

public class PaymentMultiStepRequest extends PaymentRequest {
    private String step;
    private final List<Field> fields;
    private List<String> userInputs;

    public PaymentMultiStepRequest(
            Payment payment,
            String step,
            List<Field> fields,
            List<String> userInputs) {
        super(payment);
        this.step = step;
        this.fields = fields;
        this.userInputs = userInputs;
    }

    public String getStep() {
        return step;
    }

    public List<String> getUserInputs() {
        return userInputs;
    }

}
