package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcUrlProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public class SdcDkAgent extends SdcAgent {

    @Inject
    public SdcDkAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        setJsonHttpTrafficLogsEnabled(true);
        client.setLoggingStrategy(LoggingStrategy.EXPERIMENTAL);
    }

    protected SdcApiClient constructApiClient() {
        return new SdcApiClient(
                client,
                new SdcUrlProvider(),
                persistentStorage,
                getAgentConfiguration().getProviderSpecificConfiguration(),
                getAgentConfiguration().getRedirectUrl());
    }
}
