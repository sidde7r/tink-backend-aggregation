package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.enums.SwedbankPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentStatusResponse {
    private String transactionStatus;

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public boolean isReadyForSigning() {
        return SwedbankPaymentStatus.ACCEPTED_TECHNICAL_VALIDATION.isEqualTo(transactionStatus);
    }
}
