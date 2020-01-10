package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.entity.RegisterDevice2ResponsePayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDevice2Response extends BaseResponse {

    @JsonProperty private RegisterDevice2ResponsePayload payload;

    public RegisterDevice2ResponsePayload getPayload() {
        return payload;
    }
}
