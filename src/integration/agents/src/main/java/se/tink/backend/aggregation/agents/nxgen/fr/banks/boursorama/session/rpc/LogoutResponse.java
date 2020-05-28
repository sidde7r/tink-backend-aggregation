package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.session.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LogoutResponse {
    private boolean success;

    public boolean isSuccess() {
        return success;
    }
}
