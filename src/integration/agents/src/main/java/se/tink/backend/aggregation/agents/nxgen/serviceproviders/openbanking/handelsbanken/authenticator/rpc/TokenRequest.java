package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc;

import org.codehaus.jackson.annotate.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenRequest {

    @JsonProperty("client_id")
    private String client_id;

    @JsonProperty("grant_type")
    private String grant_type;

    @JsonProperty("scope")
    private String scope;

    public TokenRequest(String client_id, String grant_type, String scope) {
        this.client_id = client_id;
        this.grant_type = grant_type;
        this.scope = scope;
    }
}
