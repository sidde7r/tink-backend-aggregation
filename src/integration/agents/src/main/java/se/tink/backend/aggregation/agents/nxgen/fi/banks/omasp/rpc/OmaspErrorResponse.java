package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OmaspErrorResponse {
    private String error;
    private String message;

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public boolean isError(String cmp) {
        return cmp.equalsIgnoreCase(getError());
    }
}
