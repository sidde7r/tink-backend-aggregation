package se.tink.backend.aggregation.nxgen.controllers.payment;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.Payment;

@Getter
public class PaymentMultiStepRequest extends PaymentRequest {
    private String step;
    private List<String> userInputs;

    public PaymentMultiStepRequest(
            Payment payment, Storage storage, String step, List<String> userInputs) {
        super(payment, storage);
        this.step = step;
        this.userInputs = userInputs;
    }

    public static PaymentMultiStepRequest of(PaymentResponse paymentResponse) {
        return new PaymentMultiStepRequest(
                paymentResponse.getPayment(),
                Storage.copyOf(paymentResponse.getStorage()),
                SigningStepConstants.STEP_INIT,
                Collections.emptyList());
    }
}
