package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.session.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class CajamarRefreshTokenResponse {
    private String accessToken;
}
