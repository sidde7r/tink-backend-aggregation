package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc;

import java.time.OffsetDateTime;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizationRequest {

    private String expires;

    private AuthorizationRequest(String expires) {
        this.expires = expires;
    }

    public String getExpires() {
        return expires;
    }

    public static AuthorizationRequest expiresInDays(int days) {
        String expires = OffsetDateTime.now().plusDays(days).toString();
        return new AuthorizationRequest(expires);
    }
}
