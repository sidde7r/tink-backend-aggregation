package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BaseErrorResponse {
    private String errorCode;
    private String error;

    @JsonProperty("aleatoire")
    private String random;

    private String from;

    @JsonProperty("msgErreur")
    private String errorMessage;

    public String getErrorCode() {
        return errorCode;
    }

    public String getError() {
        return error;
    }

    public String getRandom() {
        return random;
    }

    public String getFrom() {
        return from;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
