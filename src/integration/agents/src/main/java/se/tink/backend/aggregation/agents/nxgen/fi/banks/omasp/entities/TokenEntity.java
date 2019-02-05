package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenEntity {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("backend_timestamp")
    private Long backendTimestamp;

    public String getAccessToken() {
        return accessToken;
    }

    public Long getBackendTimestamp() {
        return backendTimestamp;
    }
}
