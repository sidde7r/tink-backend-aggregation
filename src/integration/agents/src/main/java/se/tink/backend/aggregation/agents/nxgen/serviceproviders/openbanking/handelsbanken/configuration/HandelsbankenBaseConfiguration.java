package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.UUIDConfiguration;

@JsonObject
@Getter
public class HandelsbankenBaseConfiguration implements ClientConfiguration {
    @JsonProperty @Secret @ClientIdConfiguration @UUIDConfiguration private String clientId;
}
