package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BaseResponse {

    @JsonProperty private String exitCode;

    public String getExitCode() {
        return exitCode;
    }
}
