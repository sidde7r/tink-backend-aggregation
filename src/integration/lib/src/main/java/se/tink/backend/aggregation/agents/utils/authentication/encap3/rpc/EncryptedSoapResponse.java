package se.tink.backend.aggregation.agents.utils.authentication.encap3.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EncryptedSoapResponse {
    @JsonProperty private String iv;
    @JsonProperty private String mac;
    @JsonProperty private String headers;
    @JsonProperty private String payload;

    @JsonIgnore
    public String getIv() {
        return iv;
    }

    @JsonIgnore
    public String getMac() {
        return mac;
    }

    @JsonIgnore
    public String getHeaders() {
        return headers;
    }

    @JsonIgnore
    public String getPayload() {
        return payload;
    }
}
