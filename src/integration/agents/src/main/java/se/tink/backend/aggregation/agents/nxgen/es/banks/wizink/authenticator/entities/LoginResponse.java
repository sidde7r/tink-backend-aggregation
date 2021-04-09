package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc.Result;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse {
    private GlobalPosition globalPosition;
    private Result result;

    public GlobalPosition getGlobalPosition() {
        return globalPosition;
    }

    public Result getResult() {
        return result;
    }
}
