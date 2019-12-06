package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountIbanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.InitiatedCrossBorderPaymentEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;

@JsonObject
public class CrossBorderPaymentResponse {
    private InitiatedCrossBorderPaymentEntity initiatedCrossBorderPayment;

    public PaymentResponse toTinkPayment(
            AccountIbanEntity creditor, AccountEntity debtor, PaymentStatus status) {
        return initiatedCrossBorderPayment.toTinkPayment(creditor, debtor, status);
    }

    public InitiatedCrossBorderPaymentEntity getInitiatedCrossBorderPayment() {
        return initiatedCrossBorderPayment;
    }
}
