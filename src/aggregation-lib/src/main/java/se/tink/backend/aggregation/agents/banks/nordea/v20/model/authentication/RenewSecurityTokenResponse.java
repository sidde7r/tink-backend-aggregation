package se.tink.backend.aggregation.agents.banks.nordea.v20.model.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RenewSecurityTokenResponse {

    @JsonProperty("renewSecurityTokenResponse")
    private SecurityToken securityToken;

    public SecurityToken getSecurityToken() {
        return securityToken == null ? new SecurityToken() : securityToken;
    }

    public void setSecurityToken(SecurityToken securityToken) {
        this.securityToken = securityToken;
    }
}
