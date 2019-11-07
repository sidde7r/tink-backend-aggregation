package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.AgentType;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class NordeaBaseConfiguration implements ClientConfiguration {
    @JsonProperty @Secret private String clientId;
    @JsonProperty @SensitiveSecret private String clientSecret;
    @JsonProperty @AgentConfigParam private String redirectUrl;
    @JsonProperty @AgentConfigParam private List<String> scopes;
    @JsonProperty private AgentType agentType = AgentType.PERSONAL;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public AgentType getAgentType() {
        return agentType;
    }

    public List<String> getScopes() {
        return scopes;
    }
}
