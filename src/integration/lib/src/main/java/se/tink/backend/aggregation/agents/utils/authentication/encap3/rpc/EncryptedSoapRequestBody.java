package se.tink.backend.aggregation.agents.utils.authentication.encap3.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EncryptedSoapRequestBody {
    @JsonProperty private final String keyId = "secesb";
    @JsonProperty private String iv;
    @JsonProperty private String payload;
    @JsonProperty private String headers;
    @JsonProperty private String mac;
    @JsonProperty private String emk;

    @JsonIgnore
    public void setIv(String iv) {
        this.iv = iv;
    }

    @JsonIgnore
    public void setPayload(String payload) {
        this.payload = payload;
    }

    @JsonIgnore
    public void setHeaders(String headers) {
        this.headers = headers;
    }

    @JsonIgnore
    public void setMac(String mac) {
        this.mac = mac;
    }

    @JsonIgnore
    public void setEmk(String emk) {
        this.emk = emk;
    }
}
