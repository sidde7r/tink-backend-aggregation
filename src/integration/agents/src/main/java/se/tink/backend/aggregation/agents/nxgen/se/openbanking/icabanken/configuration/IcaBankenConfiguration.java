package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;

@JsonObject
public class IcaBankenConfiguration implements ClientConfiguration {

    @JsonProperty @Secret @ClientIdConfiguration private String clientId;

    public String getClientId() {
        return clientId;
    }
}
