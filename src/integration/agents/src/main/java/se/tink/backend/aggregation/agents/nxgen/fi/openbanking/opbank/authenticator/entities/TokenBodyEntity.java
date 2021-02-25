package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities;

import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
public class TokenBodyEntity {
    private String aud;
    private String iss;
    private String response_type;
    private String client_id;
    private String redirect_uri;
    private String scope;
    private String state;
    private String nonce;
    private long max_age;
    private long exp;
    private long iat;
    private ClaimsEntity claims;
}
