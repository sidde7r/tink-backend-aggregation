package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@JsonObject
public class LoginResponse {

    @JsonProperty("AccessToken")
    private String AccessToken;

    @JsonProperty("RefreshToken")
    private String RefreshToken;

    public String getAccessToken() {

        return AccessToken;
    }

    public String getRefreshToken() {

        return RefreshToken;
    }

    public void saveResponse(SessionStorage storage) {

        storage.put(IberCajaConstants.Storage.ACCESS_TOKEN, AccessToken);
        storage.put(IberCajaConstants.Storage.REFRESH_TOKEN, RefreshToken);
    }
}
