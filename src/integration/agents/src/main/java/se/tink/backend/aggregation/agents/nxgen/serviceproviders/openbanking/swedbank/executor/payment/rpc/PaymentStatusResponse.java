package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.PaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetPaymentStatusResponse {
    private String transactionStatus;

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public boolean isReadyForSigning() {
        return transactionStatus.equalsIgnoreCase(PaymentStatus.ACTC);
    }

    public boolean isPaymentCancelled() {
        return transactionStatus.equalsIgnoreCase(PaymentStatus.CANC);
    }
}
