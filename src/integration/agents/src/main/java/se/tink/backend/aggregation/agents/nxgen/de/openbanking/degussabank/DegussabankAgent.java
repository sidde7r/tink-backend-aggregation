package se.tink.backend.aggregation.agents.nxgen.de.openbanking.degussabank;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, TRANSFERS})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.PIS_FUTURE_DATE,
            PisCapability.PIS_SEPA_RECURRING_PAYMENTS
        })
public class DegussabankAgent extends BankverlagBaseAgent {

    @Inject
    public DegussabankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, BankverlagConstants.BankverlagAspspId.DEGUSSABANK);
    }

    @Override
    protected BankverlagApiClient constructApiClient() {
        return new BankverlagApiClient(
                requestBuilder,
                bankverlagStorage,
                localDateTimeSource,
                new DegussabankErrorHandler());
    }
}
