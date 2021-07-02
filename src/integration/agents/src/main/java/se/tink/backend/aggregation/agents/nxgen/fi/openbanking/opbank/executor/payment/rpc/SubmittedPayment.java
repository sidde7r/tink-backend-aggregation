package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.enums.OpBankPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.enums.PaymentStatus;

@JsonObject
@Getter
public class SubmittedPayment {

    private String submissionId;
    private String archiveId;
    private String created;
    private String modified;
    private String status;

    public PaymentStatus getTinkStatus() {
        return OpBankPaymentStatus.fromString(status).getPaymentStatus();
    }
}
