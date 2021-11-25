package se.tink.backend.aggregation.agents.banks.se.marginalenbank;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.banks.crosskey.CrossKeyAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities(generateFromImplementedExecutors = true)
public final class MarginalenBankAgent extends CrossKeyAgent {

    @Inject
    public MarginalenBankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new MarginalenBankConfig());

        apiClient.setRootUrl("https://secure5.marginalen.se/cbs-inet-json-api-mba-v1/api/");
        apiClient.setErrorHandler(new MarginalenBankErrorHandler());
        apiClient.setAppId("1.0.0-iOS");
        apiClient.setLanguage("sv");
    }
}
