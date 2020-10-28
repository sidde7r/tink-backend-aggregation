package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private static final String INTERNAL_ERROR = "internal_error";

    private String error;

    public boolean isInternalError() {
        return INTERNAL_ERROR.equalsIgnoreCase(error);
    }
}
