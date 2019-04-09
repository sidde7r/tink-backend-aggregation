package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.entities.SecurityKeyIndexEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.rpc.LinksResponse;

public class RegisterDeviceResponse extends LinksResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    private SecurityKeyIndexEntity securityKeyIndex;

    @JsonProperty("device_token")
    private String deviceToken;

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public SecurityKeyIndexEntity getSecurityKeyIndex() {
        return securityKeyIndex;
    }

    public String getDeviceToken() {
        return deviceToken;
    }
}
