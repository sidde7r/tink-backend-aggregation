package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc;

import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Identity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class InitSessionRequest {
    private String token;
    private String deviceId;
    private String authenticationMethod;

    @JsonObject
    public static InitSessionRequest create(Sparebank1Identity identity) {
        InitSessionRequest request = new InitSessionRequest();

        request.setToken(identity.getToken());
        request.setDeviceId(identity.getDeviceId());
        request.setAuthenticationMethod("pin");

        return request;
    }
}
