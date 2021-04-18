package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sdc;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcUrlProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public class SdcNoAgent extends SdcAgent {

    @Inject
    public SdcNoAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
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
