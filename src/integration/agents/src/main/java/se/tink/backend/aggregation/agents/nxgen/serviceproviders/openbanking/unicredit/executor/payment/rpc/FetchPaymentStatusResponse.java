package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.ConsentStatusStates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.enums.UnicreditPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class FetchPaymentStatusResponse {

    private String transactionStatus;

    public PaymentResponse toTinkPayment(Payment payment) {
        payment.setStatus(UnicreditPaymentStatus.fromString(transactionStatus).getPaymentStatus());

        return new PaymentResponse(payment);
    }

    public boolean isAuthorizedPayment() {

        return ConsentStatusStates.VALID_PIS.equalsIgnoreCase(transactionStatus);
    }
}
