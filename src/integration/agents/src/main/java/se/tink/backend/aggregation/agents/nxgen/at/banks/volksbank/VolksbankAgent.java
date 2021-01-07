package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.VolksbankAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.VolksbankPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.fetcher.VolksbankCheckingAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.fetcher.VolksbankCheckingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.session.VolksbankSessionHandler;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class VolksbankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final VolksbankApiClient apiClient;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public VolksbankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        client.setUserAgent(VolksbankConstants.USER_AGENT);
        this.apiClient = VolksbankApiClient.create(persistentStorage, sessionStorage, client);

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new VolksbankPasswordAuthenticator(apiClient),
                new VolksbankAutoAuthenticator(apiClient, persistentStorage, credentials));
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

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new VolksbankCheckingAccountFetcher(apiClient, sessionStorage),
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        new VolksbankCheckingTransactionFetcher(apiClient))
                                .build()));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new VolksbankSessionHandler(apiClient);
    }
}
