package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;

@JsonObject
public class HandelsbankenBaseConfiguration implements ClientConfiguration {

    @JsonProperty @SensitiveSecret @ClientIdConfiguration private String clientId;

    public String getClientId() {
        return clientId;
    }
}
