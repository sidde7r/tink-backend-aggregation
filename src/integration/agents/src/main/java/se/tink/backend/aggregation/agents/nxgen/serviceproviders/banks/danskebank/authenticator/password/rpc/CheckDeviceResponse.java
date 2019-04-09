package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.password.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CheckDeviceResponse extends AbstractDeviceBindResponse {
    @JsonProperty("Status")
    private int status;

    @JsonProperty("Error")
    private Object error;

    public int getStatus() {
        return status;
    }

    public Object getError() {
        return error;
    }
}
