package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.MessageResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BelfiusPaymentResponse extends BelfiusResponse {
    public boolean isErrorMessageIdentifier() {
        return MessageResponse.isErrorMessageIdentifier(this);
    }

    public boolean requireSignOfBeneficiary() {
        return MessageResponse.requireSignOfBeneficiaryLimit(this);
    }
}


