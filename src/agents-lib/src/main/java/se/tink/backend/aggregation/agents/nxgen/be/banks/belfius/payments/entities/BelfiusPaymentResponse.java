package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.MessageResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BelfiusPaymentResponse extends BelfiusResponse {
    public boolean isErrorOrContinueChangeButtonDoublePayment() {
        return MessageResponse.changeButtonErrorOrDoubleMessageIdentifier(this);
    }

    public boolean requireSign() {
        return MessageResponse.requireSign(this);
    }

    public boolean requireSignOfBeneficiary() {
        return MessageResponse.requireSignOfBeneficiaryLimit(this);
    }
}


