package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.CheckPinRequestPayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CheckPinRequest {

    private CheckPinRequestPayload payload;

    public CheckPinRequest(CheckPinRequestPayload payload) {
        this.payload = payload;
    }
}
