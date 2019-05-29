package se.tink.backend.aggregation.register.nl.bunq.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.register.nl.bunq.entities.CredentialPasswordIpEntity;

@JsonObject
public class RegisterAsPSD2ProviderResponse {
    @JsonProperty("CredentialPasswordIp")
    private CredentialPasswordIpEntity credentialPasswordIp;

    public CredentialPasswordIpEntity getCredentialPasswordIp() {
        return credentialPasswordIp;
    }

    public String getToken() {
        if (credentialPasswordIp != null) {
            return credentialPasswordIp.getTokenValue();
        } else {
            throw new IllegalStateException("Object credentialPasswordIp is null.");
        }
    }
}
