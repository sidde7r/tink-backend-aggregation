package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity.TokenDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenAuthCodeResponse {
    private TokenDataEntity data;

    public TokenDataEntity getData() {
        return data;
    }
}
