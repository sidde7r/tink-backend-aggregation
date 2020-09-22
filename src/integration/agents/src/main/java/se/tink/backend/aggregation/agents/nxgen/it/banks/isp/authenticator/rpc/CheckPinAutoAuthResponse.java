package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.CheckPinResponsePayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CheckPinAutoAuthResponse {
    private CheckPinResponsePayload payload;
}
