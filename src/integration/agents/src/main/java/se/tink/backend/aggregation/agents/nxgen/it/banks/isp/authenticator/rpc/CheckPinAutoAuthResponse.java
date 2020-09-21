package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.CheckPinResponsePayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CheckPinAutoAuthResponse {
    @JsonProperty private CheckPinResponsePayload payload;
}
