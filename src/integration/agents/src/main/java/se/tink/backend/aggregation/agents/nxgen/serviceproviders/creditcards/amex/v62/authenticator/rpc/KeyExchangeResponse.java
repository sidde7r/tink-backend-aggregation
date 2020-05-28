package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeyExchangeResponse {
    @JsonProperty("status_code_type")
    private String statusCodeType;

    @JsonProperty("status_code")
    private String statusCode;

    @JsonProperty("encrypted_keys")
    private String encryptedKeys;

    @JsonProperty("device_key_version")
    private String deviceKeyVersion;

    public String getStatusCodeType() {
        return statusCodeType;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getEncryptedKeys() {
        return encryptedKeys;
    }

    public String getDeviceKeyVersion() {
        return deviceKeyVersion;
    }
}
