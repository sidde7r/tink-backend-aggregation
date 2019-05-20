package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.entities.SecurityKeyIndexEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc.OmaspBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@SuppressWarnings("unused")
public class RegisterDeviceResponse extends OmaspBaseResponse {

    @JsonProperty("device_token")
    private String deviceToken;

    private SecurityKeyIndexEntity securityKeyIndexEntity;

    public String getDeviceToken() {
        return deviceToken;
    }

    public SecurityKeyIndexEntity getSecurityKeyIndexEntity() {
        return securityKeyIndexEntity;
    }
}
