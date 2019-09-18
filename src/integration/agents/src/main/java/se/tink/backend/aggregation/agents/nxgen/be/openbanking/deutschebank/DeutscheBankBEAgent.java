package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.configuration.DeutscheBankBEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.DeutscheBankMultifactorAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.DeutscheBankTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class DeutscheBankBEAgent extends DeutscheBankAgent {

    private final DeutscheBankConfiguration deutscheBankBEConfiguration;
    private final DeutscheBankApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public DeutscheBankBEAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        super(request, context, configuration);
        deutscheBankBEConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(DeutscheBankBEConfiguration.class);
        apiClient =
                new DeutscheBankBEApiClient(client, sessionStorage, deutscheBankBEConfiguration);
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final DeutscheBankMultifactorAuthenticator deutscheBankAuthenticatorController =
                new DeutscheBankMultifactorAuthenticator(
                        apiClient,
                        sessionStorage,
                        credentials.getField(DeutscheBankConstants.CredentialKeys.IBAN),
                        credentials.getField(DeutscheBankConstants.CredentialKeys.PSU_ID),
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                context,
                deutscheBankAuthenticatorController,
                deutscheBankAuthenticatorController);
    }

    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final DeutscheBankTransactionalAccountFetcher accountFetcher =
                new DeutscheBankTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(accountFetcher)));
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }
}
