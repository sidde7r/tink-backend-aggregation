package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BaseResponse {
    @JsonProperty("codeRetour")
    private int returnCode;
    private String message;

    public int getReturnCode() {
        return returnCode;
    }

    public String getMessage() {
        return message;
    }

    public void assertReturnCodeOk() {
        if (returnCode != 0) {
            throw new IllegalStateException(
                    "Something went wrong with the request, did not receive an ok response");
        }
    }
}
