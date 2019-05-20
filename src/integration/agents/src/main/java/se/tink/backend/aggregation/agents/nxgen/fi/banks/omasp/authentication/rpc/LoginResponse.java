package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.entities.SecurityKeyIndexEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc.OmaspBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@SuppressWarnings("unused")
public class LoginResponse extends OmaspBaseResponse {

    private String name;
    private SecurityKeyIndexEntity securityKeyIndex;
    private Boolean securityKeyRequired;
    private Boolean passwordChangeRequired;

    // Yes, their naming case is inconsistent
    @JsonProperty("device_token")
    private String deviceToken;

    public SecurityKeyIndexEntity getSecurityKeyIndex() {
        return securityKeyIndex;
    }

    public Boolean getSecurityKeyRequired() {
        return securityKeyRequired;
    }

    public Boolean getPasswordChangeRequired() {
        return passwordChangeRequired;
    }

    public String getName() {
        return name;
    }

    public String getDeviceToken() {
        return deviceToken;
    }
}
