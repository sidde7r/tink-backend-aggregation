package se.tink.backend.aggregation.agents.nxgen.se.business.nordea;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.NordeaSEBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.NordeaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.NordeaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.rpc.filter.NordeaSEFilter;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.session.NordeaSESessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.identitydata.IdentityData;

public class NordeaSEAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor {
    private final NordeaSEApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final NordeaSEBankIdAuthenticator nordeaSEBankIdAuthenticator;

    @Inject
    public NordeaSEAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        client.addFilter(new NordeaSEFilter());
        apiClient = new NordeaSEApiClient(client, sessionStorage);
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        nordeaSEBankIdAuthenticator =
                new NordeaSEBankIdAuthenticator(
                        apiClient,
                        sessionStorage,
                        Optional.ofNullable(
                                        componentProvider
                                                .getCredentialsRequest()
                                                .getCredentials()
                                                .getField(Key.CORPORATE_ID))
                                .map(s -> s.replace("-", ""))
                                .map(String::trim)
                                .orElse(""));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                supplementalRequester, nordeaSEBankIdAuthenticator, persistentStorage, credentials);
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

    @Override
    protected SessionHandler constructSessionHandler() {
        return new NordeaSESessionHandler(sessionStorage);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        final NordeaTransactionFetcher transactionFetcher = new NordeaTransactionFetcher(apiClient);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new NordeaTransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                IdentityData.builder()
                        .setFullName(sessionStorage.get(StorageKeys.HOLDER_NAME))
                        .setDateOfBirth(null)
                        .build());
    }
}
