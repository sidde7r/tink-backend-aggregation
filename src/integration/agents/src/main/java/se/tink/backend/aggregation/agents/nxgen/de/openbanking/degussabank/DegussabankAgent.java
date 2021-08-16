package se.tink.backend.aggregation.agents.nxgen.de.openbanking.degussabank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
})
public class DegussabankAgent extends BankverlagBaseAgent {

    @Inject
    public DegussabankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, BankverlagConstants.BankverlagAspspId.DEGUSSABANK);
    }

    @Override
    protected BankverlagApiClient constructApiClient() {
        return new BankverlagApiClient(
                client,
                constructHeaderValues(),
                bankverlagStorage,
                randomValueGenerator,
                localDateTimeSource,
                new DegussabankErrorHandler());
    }
}
