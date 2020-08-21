package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.CheckTimeResponsePayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CheckTimeResponse {

    @JsonProperty("CheckTime")
    CheckTimeResponsePayload payload;

    public CheckTimeResponsePayload getPayload() {
        return payload;
    }
}
