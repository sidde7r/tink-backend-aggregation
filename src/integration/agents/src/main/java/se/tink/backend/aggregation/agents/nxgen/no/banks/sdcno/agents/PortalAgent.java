package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.agents;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoAgent;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard.CreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard.CreditCardTransactionFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
public final class PortalAgent extends SdcNoAgent implements RefreshCreditCardAccountsExecutor {

    private final CreditCardRefreshController creditCardRefreshController;

    @Inject
    public PortalAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        creditCardRefreshController = constructCreditCardRefreshController();
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        return new CreditCardRefreshController(
                this.metricRefreshController,
                this.updateController,
                new CreditCardFetcher(bankClient),
                new CreditCardTransactionFetcher(bankClient));
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }
}
