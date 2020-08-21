package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CheckTimeResponsePayload {
    @JsonProperty("DeviceStatus")
    private String status;

    @JsonProperty("DifferenceInSeconds")
    private String differenceInSeconds;

    @JsonProperty("ErrorMsg")
    private String errorMsg;

    @JsonProperty("ReturnCode")
    private String returnCode;

    @JsonProperty("ServerUTCEpoch")
    private String serverUTCEpoch;

    public String getStatus() {
        return status;
    }

    public String getDifferenceInSeconds() {
        return differenceInSeconds;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public String getServerUTCEpoch() {
        return serverUTCEpoch;
    }
}
