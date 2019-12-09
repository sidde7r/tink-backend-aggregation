package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.payment.executor.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.ConsentStatusStates;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentStatusResponse {

    private String transactionStatus;

    public boolean isAuthorizedPayment() {

        return ConsentStatusStates.VALID_PIS.equalsIgnoreCase(transactionStatus);
    }
}
