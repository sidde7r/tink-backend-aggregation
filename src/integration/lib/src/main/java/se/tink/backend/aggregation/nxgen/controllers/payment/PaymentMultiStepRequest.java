package se.tink.backend.aggregation.nxgen.controllers.payment;

import java.util.Collections;
import java.util.List;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.libraries.payment.rpc.Payment;

public class PaymentMultiStepRequest extends PaymentRequest {
    private String step;
    private final List<Field> fields;
    private List<String> userInputs;

    public PaymentMultiStepRequest(
            Payment payment, String step, List<Field> fields, List<String> userInputs) {
        super(payment);
        this.step = step;
        this.fields = fields;
        this.userInputs = userInputs;
    }

    public static PaymentMultiStepRequest of(PaymentResponse paymentResponse) {
        return new PaymentMultiStepRequest(
                paymentResponse.getPayment(),
                AuthenticationStepConstants.STEP_INIT,
                Collections.emptyList(),
                Collections.emptyList());
    }

    public String getStep() {
        return step;
    }

    public List<String> getUserInputs() {
        return userInputs;
    }
}
