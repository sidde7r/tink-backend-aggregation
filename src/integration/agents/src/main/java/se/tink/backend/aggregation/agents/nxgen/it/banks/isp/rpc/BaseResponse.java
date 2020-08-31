package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BaseResponse {

    private static final String OK_CODE = "OK";

    @JsonProperty private String exitCode;

    public boolean isOk() {
        return OK_CODE.equals(exitCode);
    }
}
