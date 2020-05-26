package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {

    private String error;

    public boolean isInvalidGrant() {
        return ErrorMessages.INVALID_TOKEN.contains(error);
    }
}
