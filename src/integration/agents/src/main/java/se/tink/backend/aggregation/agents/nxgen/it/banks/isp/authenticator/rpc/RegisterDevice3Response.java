package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.RegisterDevice3ResponsePayload;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDevice3Response extends BaseResponse {
    private RegisterDevice3ResponsePayload payload;

    public RegisterDevice3ResponsePayload getPayload() {
        return payload;
    }
}
