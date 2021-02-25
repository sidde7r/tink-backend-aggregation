package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.authenticator.CajamarAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.account.CajamarAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.account.CajamarAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.CajamarCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.CajamarCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.session.CajamarSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@Slf4j
@AgentCapabilities({CHECKING_ACCOUNTS, CREDIT_CARDS})
public class CajamarAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshCreditCardAccountsExecutor {

    private final CajamarApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;

    @Inject
    protected CajamarAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        this.apiClient = new CajamarApiClient(client, sessionStorage);
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        this.creditCardRefreshController = constructCreditCardRefreshController();
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
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new CajamarAuthenticator(apiClient);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new CajamarSessionHandler(apiClient);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new CajamarAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new CajamarAccountTransactionFetcher(apiClient))));
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new CajamarCreditCardFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new CajamarCreditCardTransactionFetcher(apiClient))));
    }
}
