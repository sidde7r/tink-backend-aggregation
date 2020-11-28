package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.authenticator.MinPensionAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.fetcher.pension.PensionAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.session.MinPensionSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({INVESTMENTS})
public class MinPensionAgent extends NextGenerationAgent
        implements RefreshInvestmentAccountsExecutor, RefreshIdentityDataExecutor {
    private final MinPensionApiClient minPensionApiClient;
    private final InvestmentRefreshController investmentRefreshController;

    @Inject
    public MinPensionAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        client.disableAggregatorHeader();
        client.disableSignatureRequestHeader();
        minPensionApiClient = new MinPensionApiClient(client, sessionStorage);

        final PensionAccountFetcher pensionAccountFetcher =
                new PensionAccountFetcher(minPensionApiClient);
        investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController, updateController, pensionAccountFetcher);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                context,
                new MinPensionAuthenticator(minPensionApiClient, sessionStorage),
                persistentStorage,
                credentials);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new MinPensionSessionHandler(minPensionApiClient);
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return investmentRefreshController.fetchInvestmentTransactions();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return null;
    }
}
