package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.spankki;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.spankki.fetcher.SPankkiFITransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration.CrosskeyMarketConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, TRANSFERS})
@AgentPisCapability(capabilities = {PisCapability.SEPA_CREDIT_TRANSFER})
public final class SPankkiAgent extends CrosskeyBaseAgent
        implements RefreshTransferDestinationExecutor {

    private static final CrosskeyMarketConfiguration SPANKKI_CONFIGURATION =
            new CrosskeyMarketConfiguration(
                    "s-pankki",
                    "https://api.s-pankki.fi",
                    "https://openbanking.s-pankki.fi",
                    "api.s-pankki.fi");

    @Inject
    public SPankkiAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, SPANKKI_CONFIGURATION);
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return constructTransferDestinationController().fetchTransferDestinations(accounts);
    }

    private TransferDestinationRefreshController constructTransferDestinationController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new SPankkiFITransferDestinationFetcher());
    }
}
