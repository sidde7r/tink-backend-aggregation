package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid;

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
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.accounts.checkingaccount.DnbAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.accounts.checkingaccount.DnbTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.accounts.creditcardaccount.DnbCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.accounts.creditcardaccount.DnbCreditTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.authenticator.DnbAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.authenticator.DnbBankIdIframeInitializer;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.filters.DnbRedirectFilter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.filters.DnbRetryFilter;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticationControllerProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticationControllerProviderModule;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelperFactory;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
@AgentDependencyModules(modules = BankIdIframeAuthenticationControllerProviderModule.class)
public final class DnbAgent extends NextGenerationAgent
        implements RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    private final BankIdIframeAuthenticationControllerProvider authenticationControllerProvider;

    private final DnbApiClient apiClient;
    private final DnbAccountFetcher accountFetcher;
    private final DnbTransactionFetcher transactionFetcher;
    private final DnbCreditCardFetcher creditCardFetcher;
    private final DnbCreditTransactionFetcher creditTransactionFetcher;

    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public DnbAgent(
            AgentComponentProvider componentProvider,
            BankIdIframeAuthenticationControllerProvider authenticationControllerProvider) {
        super(componentProvider);

        this.authenticationControllerProvider = authenticationControllerProvider;

        configureHttpClient(client);
        this.apiClient = new DnbApiClient(client);
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
        DnbBankIdIframeInitializer iframeInitializer = new DnbBankIdIframeInitializer(credentials);
        DnbAuthenticator dnbAuthenticator = new DnbAuthenticator(apiClient);
        return authenticationControllerProvider.createAuthController(
                catalog,
                context,
                supplementalInformationController,
                iframeInitializer,
                dnbAuthenticator);
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
                new TransactionPaginationHelperFactory(configuration).create(request);
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
        return SessionHandler.alwaysFail();
    }
}
