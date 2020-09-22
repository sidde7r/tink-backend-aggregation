package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.BoursoramaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.AddBeneficiaryExecutor;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.identity.BoursoramaIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount.BoursoramaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount.BoursoramaTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.session.BoursoramaSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.storage.BoursoramaPersistentStorage;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class BoursoramaAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor {
    private final BoursoramaApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final BoursoramaPersistentStorage boursoramaPersistentStorage;

    @Inject
    public BoursoramaAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);

        boursoramaPersistentStorage = new BoursoramaPersistentStorage(persistentStorage);
        apiClient = new BoursoramaApiClient(client, boursoramaPersistentStorage, sessionStorage);
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        BoursoramaTransactionalAccountFetcher accountFetcher =
                new BoursoramaTransactionalAccountFetcher(apiClient);

        BoursoramaTransactionalAccountTransactionFetcher transactionFetcher =
                new BoursoramaTransactionalAccountTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper, transactionFetcher));
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new BoursoramaIdentityDataFetcher(apiClient).fetchIdentityData();
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
    public Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new BoursoramaAuthenticator(apiClient, boursoramaPersistentStorage));
    }

    @Override
    public SessionHandler constructSessionHandler() {
        return new BoursoramaSessionHandler(apiClient);
    }

    @Override
    public Optional<CreateBeneficiaryController> constructCreateBeneficiaryController() {
        return Optional.of(
                new CreateBeneficiaryController(
                        new AddBeneficiaryExecutor(
                                this.apiClient,
                                this.sessionStorage,
                                this.supplementalInformationHelper)));
    }
}
