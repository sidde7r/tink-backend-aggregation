package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDevice3RequestPayload {

    @JsonProperty("deviceId")
    private String deviceId;

    @JsonProperty("pin")
    private String newPin;

    @JsonProperty("trxid")
    private String transactionId;

    public RegisterDevice3RequestPayload(
            final String deviceId, final String newPin, final String transactionId) {
        this.deviceId = Objects.requireNonNull(deviceId);
        this.newPin = Objects.requireNonNull(newPin);
        this.transactionId = Objects.requireNonNull(transactionId);
    }
}
