package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities.DeviceDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeyExchangeRequest {
    @JsonProperty("device_data")
    private DeviceDataEntity deviceData;

    @JsonProperty("device_random_number")
    private String deviceRandomNumber;

    public KeyExchangeRequest(String deviceRandomNumber) {
        this.deviceRandomNumber = deviceRandomNumber;
        this.deviceData = new DeviceDataEntity();
    }
}
