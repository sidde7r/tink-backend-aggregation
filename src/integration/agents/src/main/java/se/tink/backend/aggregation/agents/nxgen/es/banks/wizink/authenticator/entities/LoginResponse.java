package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse extends BaseResponse {
    private GlobalPosition globalPosition;

    public GlobalPosition getGlobalPosition() {
        return globalPosition;
    }
}
