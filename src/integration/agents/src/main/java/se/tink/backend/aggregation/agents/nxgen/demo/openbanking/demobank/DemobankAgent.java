package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.configuration.DemobankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.DemobankTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class DemobankAgent extends NextGenerationAgent implements RefreshCheckingAccountsExecutor {

    protected final DemobankApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public DemobankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        apiClient = new DemobankApiClient(client, sessionStorage);
        apiClient.setConfiguration(getClientConfiguration());
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
                        new TransactionKeyPaginationController<>(
                                demobankTransactionalAccountFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SessionHandler() {
            @Override
            public void logout() {
                // nop.
            }

            @Override
            public void keepAlive() throws SessionException {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        };
    }

    @Override
    protected Authenticator constructAuthenticator() {
        //        // Authenticator will need:
        //        // 1. Callback URI -- needs to be fetched from credentials request?
        //        // 2. Authorize URI -- uri that we will open to get the user to do SCA on
        //        // 3. Authorization code -- Tink's authorization code.
        //
        //        DemobankRedirectAuthenticator demobankRedirectAuthenticator =
        //                new DemobankRedirectAuthenticator(apiClient, persistentStorage,
        // credentials);
        //
        //        final OAuth2AuthenticationController controller =
        //                new OAuth2AuthenticationController(
        //                        persistentStorage,
        //                        supplementalInformationHelper,
        //                        demobankRedirectAuthenticator,
        //                        credentials,
        //                        strongAuthenticationState);
        //
        //        return new AutoAuthenticationController(
        //                request,
        //                context,
        //                new ThirdPartyAppAuthenticationController<>(
        //                        controller, supplementalInformationHelper),
        //                controller);

        return new PasswordAuthenticationController(
                new DemobankPasswordAuthenticator(sessionStorage, apiClient));
    }

    protected DemobankConfiguration getClientConfiguration() {
        DemobankConfiguration demobankConfiguration = new DemobankConfiguration();
        return demobankConfiguration;
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
