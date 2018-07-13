package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.entities.TokenEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OmaspBaseResponse {
    private TokenEntity token;

    public TokenEntity getToken() {
        return token;
    }
}
