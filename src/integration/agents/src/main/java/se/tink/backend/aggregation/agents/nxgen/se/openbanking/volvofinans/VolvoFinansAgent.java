package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.authenticator.VolvoFinansAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.configuration.VolvoFinansConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.fetcher.transactionalaccount.VolvoFinansCreditCardAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.fetcher.transactionalaccount.VolvoFinansCreditCardTransactionsFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class VolvoFinansAgent extends NextGenerationAgent
        implements RefreshCreditCardAccountsExecutor {

    private final VolvoFinansApiClient apiClient;
    private final CreditCardRefreshController creditCardRefreshController;

    public VolvoFinansAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());

        apiClient = new VolvoFinansApiClient(client, persistentStorage);

        this.creditCardRefreshController = getCreditCardRefreshController();

        apiClient.setConfiguration(
                getClientConfiguration(), agentsServiceConfiguration.getEidasProxy());
        this.client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
    }

    protected VolvoFinansConfiguration getClientConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(VolvoFinansConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new VolvoFinansAuthenticator(
                                apiClient, persistentStorage, getClientConfiguration()),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController getCreditCardRefreshController() {
        final VolvoFinansCreditCardAccountsFetcher creditCardFetcher =
                new VolvoFinansCreditCardAccountsFetcher(apiClient);

        final VolvoFinansCreditCardTransactionsFetcher transactionsFetcher =
                new VolvoFinansCreditCardTransactionsFetcher(apiClient);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(transactionsFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
