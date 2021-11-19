package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.DnbAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.DnbTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.DnbCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.DnbCreditTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator.DnbAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.filters.DnbRedirectFilter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.filters.DnbRetryFilter;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticationControllerNO;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelperFactory;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
public final class DnbAgent extends NextGenerationAgent
        implements RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    private final DnbApiClient apiClient;
    private final DnbAuthenticator authenticator;
    private final DnbAccountFetcher accountFetcher;
    private final DnbTransactionFetcher transactionFetcher;
    private final DnbCreditCardFetcher creditCardFetcher;
    private final DnbCreditTransactionFetcher creditTransactionFetcher;

    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public DnbAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        configureHttpClient(client);
        this.apiClient = new DnbApiClient(client);
        this.authenticator = new DnbAuthenticator(apiClient);
        this.accountFetcher = new DnbAccountFetcher(apiClient);
        this.transactionFetcher = new DnbTransactionFetcher(apiClient);
        this.creditCardFetcher = new DnbCreditCardFetcher(apiClient);
        this.creditTransactionFetcher = new DnbCreditTransactionFetcher(apiClient);

        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.setFollowRedirects(false);
        client.addFilter(new DnbRedirectFilter());
        client.addFilter(
                new TimeoutRetryFilter(
                        DnbConstants.TimeoutFilter.NUM_TIMEOUT_RETRIES,
                        DnbConstants.TimeoutFilter.TIMEOUT_RETRY_SLEEP_MILLISECONDS));
        client.addFilter(
                new DnbRetryFilter(
                        DnbConstants.RetryFilter.NUM_TIMEOUT_RETRIES,
                        DnbConstants.RetryFilter.RETRY_SLEEP_MILLISECONDS));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(
                request,
                context,
                new BankIdAuthenticationControllerNO(
                        supplementalInformationController, authenticator, catalog),
                authenticator);
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
        TransactionPaginationHelper transactionPaginationHelper =
                new TransactionPaginationHelperFactory().create(request);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper, transactionFetcher));
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                creditTransactionFetcher);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new DnbSessionHandler(apiClient);
    }
}
