package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OmaspErrorResponse {

    private static final String PASSWORD = "password";

    private String error;
    private String message;
    private String reason;

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public boolean isError(String cmp) {
        return cmp.equalsIgnoreCase(getError());
    }

    public boolean isPasswordError() {
        return (message != null && message.contains(PASSWORD))
                || (reason != null && reason.contains(PASSWORD));
    }
}
