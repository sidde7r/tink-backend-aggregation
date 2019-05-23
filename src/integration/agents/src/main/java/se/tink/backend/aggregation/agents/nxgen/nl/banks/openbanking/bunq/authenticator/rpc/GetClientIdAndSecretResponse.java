package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.entities.OauthClientEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetClientIdAndSecretResponse {
    @JsonProperty("OauthClient")
    private OauthClientEntity oauthClient;

    public OauthClientEntity getOauthClient() {
        return oauthClient;
    }
}
