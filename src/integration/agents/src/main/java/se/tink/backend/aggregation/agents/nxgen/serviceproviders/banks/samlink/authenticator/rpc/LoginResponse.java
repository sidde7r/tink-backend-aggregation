package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.entities.SecurityKeyIndexEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.rpc.LinksResponse;

public class LoginResponse extends LinksResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    private String name;
    private String lastLoginTime;
    private SecurityKeyIndexEntity securityKeyIndex;

    @JsonProperty("device_token")
    private String deviceToken;

    private String encryptedUsername;

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getName() {
        return name;
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public SecurityKeyIndexEntity getSecurityKeyIndex() {
        return securityKeyIndex;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public String getEncryptedUsername() {
        return encryptedUsername;
    }
}
