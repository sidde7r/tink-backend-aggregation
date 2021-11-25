package se.tink.backend.aggregation.agents.banks.se.alandsbanken;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.banks.crosskey.CrossKeyAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities(generateFromImplementedExecutors = true)
public final class AlandsBankenAgent extends CrossKeyAgent {

    @Inject
    public AlandsBankenAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new AlandsBankenConfig());

        apiClient.setRootUrl("https://mob.alandsbanken.se/cbs-inet-json-api-abs-v1/api/");
        apiClient.setErrorHandler(new AlandsBankenErrorHandler());
        apiClient.setLanguage("sv");

        apiClient.setAppId("1.4.0-iOS");
    }
}
