package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc;

import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentType;

public interface CreatePaymentResponse {

    String getPaymentId();

    String getScaRedirect();

    PaymentResponse toTinkPayment(
            String debtorAccountNumber, String creditorAccountNumber, PaymentType type);
}
