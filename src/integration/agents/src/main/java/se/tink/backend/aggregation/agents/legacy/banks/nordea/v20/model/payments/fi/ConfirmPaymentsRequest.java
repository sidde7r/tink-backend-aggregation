package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.fi;

import com.google.common.collect.Lists;
import java.util.List;

public class ConfirmPaymentsRequest {

    private ConfirmPaymentsIn confirmPaymentsIn;

    public ConfirmPaymentsRequest(String paymentId, String confirmationCode, String challenge) {

        ConfirmPayment payment = new ConfirmPayment();
        payment.setType("Domestic");
        payment.setPaymentSubTypeExtension("FIType");
        payment.setPaymentId(paymentId);

        List<ConfirmPayment> payments = Lists.newArrayList();
        payments.add(payment);

        confirmPaymentsIn = new ConfirmPaymentsIn();
        confirmPaymentsIn.setChallenge(challenge);
        confirmPaymentsIn.setConfirmationCode(confirmationCode);
        confirmPaymentsIn.setPayment(payments);
    }

    public ConfirmPaymentsIn getConfirmPaymentsIn() {
        return confirmPaymentsIn;
    }

    public void setConfirmPaymentsIn(ConfirmPaymentsIn confirmPaymentsIn) {
        this.confirmPaymentsIn = confirmPaymentsIn;
    }
}
