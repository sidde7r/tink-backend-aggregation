package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.entity.RefreshEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class RefreshRequest { //TODO: USE FORM
    @JsonProperty("refresh_token")
    private RefreshEntity refreshEntity;

    public RefreshRequest(String refreshToken) {
        this.refreshEntity = new RefreshEntity(refreshToken);
    }

    public String toTinkRefresh() {
        return SerializationUtils.serializeToString(refreshEntity);
    }
}
