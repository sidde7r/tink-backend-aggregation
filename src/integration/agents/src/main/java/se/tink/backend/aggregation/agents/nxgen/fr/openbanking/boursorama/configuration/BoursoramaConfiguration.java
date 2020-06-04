package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.configuration;

import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class BoursoramaConfiguration implements ClientConfiguration {

    @Secret private String clientId;

    @AgentConfigParam private String baseUrl;
    @AgentConfigParam private String qsealKeyUrl;

    public String getClientId() {
        return clientId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getQsealKeyUrl() {
        return qsealKeyUrl;
    }
}
