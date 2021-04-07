package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants.HttpClientParams;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants.TimeoutRetryFilterParams;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.BankdataNemIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.BankdataPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataIdentitydataFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.filter.KnownErrorsFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.storage.BankdataStorage;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameControllerInitializer;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;
import se.tink.libraries.credentials.service.CredentialsRequest;

@Slf4j
public class BankdataAgent extends NextGenerationAgent
        implements RefreshInvestmentAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshIdentityDataExecutor {

    private static final String STATIC_SALT = "Aiceimee;l9ikaesae1U";
    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    private final BankdataApiClient bankClient;
    private final InvestmentRefreshController investmentRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final StatusUpdater statusUpdater;

    public BankdataAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        bankClient = new BankdataApiClient(client, request.getProvider());

        investmentRefreshController = constructInvestmentRefreshController();
        creditCardRefreshController = constructCreditCardRefreshController();
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        loanRefreshController = constructLoanRefreshController();

        statusUpdater = context;
    }

    private InvestmentRefreshController constructInvestmentRefreshController() {
        BankdataInvestmentFetcher investmentFetcher = new BankdataInvestmentFetcher(bankClient);
        return new InvestmentRefreshController(
                metricRefreshController, updateController, investmentFetcher);
    }

    protected void configureHttpClient(TinkHttpClient client) {
        // Known bankdata errors
        client.addFilter(constructKnowErrorsFilter());
        // Catches the various timeouts, one of them is "connect timeout" after it is retried few
        // times in filter below
        client.addFilter(new TimeoutFilter());
        // Tries few times in case of SocketTimeoutException
        client.addFilter(
                new TimeoutRetryFilter(
                        TimeoutRetryFilterParams.NUM_TIMEOUT_RETRIES,
                        TimeoutRetryFilterParams.TIMEOUT_RETRY_SLEEP_MILLISECONDS));
        client.setTimeout(HttpClientParams.CLIENT_TIMEOUT);
    }

    protected Filter constructKnowErrorsFilter() {
        return new KnownErrorsFilter();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BankdataNemIdAuthenticator nemIdAuthenticator =
                new BankdataNemIdAuthenticator(bankClient, persistentStorage);

        NemIdAuthenticationController nemidAuthenticationController =
                new NemIdAuthenticationController(
                        NemIdIFrameControllerInitializer.initNemIdIframeController(
                                nemIdAuthenticator,
                                catalog,
                                statusUpdater,
                                supplementalInformationController,
                                metricContext),
                        nemIdAuthenticator,
                        persistentStorage);

        BankdataStorage bankdataPersistentStorage = new BankdataStorage(persistentStorage);
        BankdataPasswordAuthenticator passwordAuthenticator =
                new BankdataPasswordAuthenticator(
                        credentials.getField(Key.USERNAME),
                        credentials.getField(Key.ACCESS_PIN),
                        nemIdAuthenticator,
                        bankdataPersistentStorage);

        logHashes();
        return new AutoAuthenticationController(
                request, systemUpdater, nemidAuthenticationController, passwordAuthenticator);
    }

    private void logHashes() {
        // There are a lot of invalid_credentials thrown.
        // Users often finally manages to provide correct credentials in 2nd or 3rd attempt.
        // We want to investigate if users have problems with providing username or password.
        // To achieve that - this logging will be helpful. We will check the hashes from
        // unsuccessful and successful authentications for the same credentialsId / userId and check
        // whether username hash or credentials hash changed.
        log.info(
                "[Bankdata NemId] Hashes: {}, {}, {}",
                ENCODER.encodeToString(
                                Hash.sha512(credentials.getField(Key.USERNAME) + STATIC_SALT))
                        .substring(0, 6),
                ENCODER.encodeToString(
                                Hash.sha512(credentials.getField(Key.PASSWORD) + STATIC_SALT))
                        .substring(0, 6),
                ENCODER.encodeToString(
                                Hash.sha512(credentials.getField(Key.ACCESS_PIN) + STATIC_SALT))
                        .substring(0, 6));
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
        BankdataTransactionFetcher transactionFetcher = new BankdataTransactionFetcher(bankClient);
        BankdataTransactionalAccountFetcher accountFetcher =
                new BankdataTransactionalAccountFetcher(bankClient);

        TransactionPagePaginationController<TransactionalAccount>
                transactionPagePaginationController =
                        new TransactionPagePaginationController<>(
                                transactionFetcher, BankdataConstants.Fetcher.START_PAGE);

        TransactionFetcherController<TransactionalAccount> transactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        transactionPagePaginationController,
                        transactionFetcher);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                transactionFetcherController);
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
        BankdataCreditCardAccountFetcher ccAccountFetcher =
                new BankdataCreditCardAccountFetcher(bankClient);
        BankdataCreditCardTransactionFetcher ccTransactionFetcher =
                new BankdataCreditCardTransactionFetcher(bankClient);

        TransactionPagePaginationController<CreditCardAccount>
                ccTransactionPagePaginationController =
                        new TransactionPagePaginationController<>(
                                ccTransactionFetcher, BankdataConstants.Fetcher.START_PAGE);

        TransactionFetcherController<CreditCardAccount> ccTransactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper, ccTransactionPagePaginationController);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                ccAccountFetcher,
                ccTransactionFetcherController);
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return investmentRefreshController.fetchInvestmentTransactions();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    private LoanRefreshController constructLoanRefreshController() {
        BankdataLoanFetcher loanFetcher = new BankdataLoanFetcher(bankClient);
        TransactionFetcherController<LoanAccount> transactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                loanFetcher, BankdataConstants.Fetcher.START_PAGE));

        return new LoanRefreshController(
                metricRefreshController,
                updateController,
                loanFetcher,
                transactionFetcherController);
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                new BankdataIdentitydataFetcher(persistentStorage).fetchIdentityData());
    }
}
