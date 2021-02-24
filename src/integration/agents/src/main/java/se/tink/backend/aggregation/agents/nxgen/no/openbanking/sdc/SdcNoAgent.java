package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sdc;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcUrlProvider;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public class SdcNoAgent extends SdcAgent {

    public SdcNoAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration);
    }

    protected SdcApiClient constructApiClient() {
        return new SdcNoApiClient(
                client,
                new SdcUrlProvider(),
                persistentStorage,
                getAgentConfiguration().getProviderSpecificConfiguration(),
                getAgentConfiguration().getRedirectUrl());
    }
}
