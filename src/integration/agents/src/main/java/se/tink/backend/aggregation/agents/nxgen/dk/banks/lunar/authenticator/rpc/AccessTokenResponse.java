package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AccessTokenResponse {
    private String accessToken;
    private String guid;
    private String intercomHmac;
}
