package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;

@JsonObject
@Getter
public class LuminorConfiguration implements ClientConfiguration {

    @JsonProperty @Secret @ClientIdConfiguration private String clientId;
}
