package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.ClusterIds;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.ClusterSpecificCallbacks;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.ProviderNameRegex;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankRedirectAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.DemobankTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class DemobankAgent extends NextGenerationAgent implements RefreshCheckingAccountsExecutor {

    protected final DemobankApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public DemobankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        String callbackUri = request.getCallbackUri();
        if (callbackUri == null) {
            switch (context.getClusterId()) {
                case ClusterIds.OXFORD_STAGING:
                    callbackUri = ClusterSpecificCallbacks.OXFORD_STAGING_CALLBACK;
                    break;
                case ClusterIds.OXFORD_PREPROD:
                    callbackUri = ClusterSpecificCallbacks.OXFORD_PREPROD_CALLBACK;
                    break;
                default:
                    callbackUri = ClusterSpecificCallbacks.OXFORD_PROD_CALLBACK;
            }
        }
        apiClient = new DemobankApiClient(client, sessionStorage, callbackUri);
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        final DemobankTransactionalAccountFetcher demobankTransactionalAccountFetcher =
                new DemobankTransactionalAccountFetcher(apiClient, sessionStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                demobankTransactionalAccountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                demobankTransactionalAccountFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        // Check if this can be done in a better way than using condition on provider-name

        if (credentials.getProviderName().matches(ProviderNameRegex.PASSWORD_PROVIDER)) {
            return new PasswordAuthenticationController(
                    new DemobankPasswordAuthenticator(apiClient));
        } else {
            DemobankRedirectAuthenticator demobankRedirectAuthenticator =
                    new DemobankRedirectAuthenticator(apiClient, persistentStorage, credentials);

            final OAuth2AuthenticationController controller =
                    new OAuth2AuthenticationController(
                            persistentStorage,
                            supplementalInformationHelper,
                            demobankRedirectAuthenticator,
                            credentials,
                            strongAuthenticationState);

            return new AutoAuthenticationController(
                    request,
                    context,
                    new ThirdPartyAppAuthenticationController<>(
                            controller, supplementalInformationHelper),
                    controller);
        }
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }
}
