package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RefreshEntity {
    @JsonProperty("grant_type")
    private String grantType = RaiffeisenConstants.BODY.GRANT_TYPE_REFRESH_TOKEN;
    @JsonProperty("refresh_token")
    private String refreshToken;

    public RefreshEntity(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
