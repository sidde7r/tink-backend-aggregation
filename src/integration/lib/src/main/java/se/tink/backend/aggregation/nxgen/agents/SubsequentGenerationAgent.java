package se.tink.backend.aggregation.nxgen.agents;

import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.ProgressiveAuthAgent;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.SuperAbstractAgent;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutorNxgen;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.constants.MarketCode;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.LoadedAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountRefresher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.Refresher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.TransactionRefresher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.UpdateController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.ClientFilterFactory;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.transfer.rpc.Transfer;

/**
 * Same as the old NextGenerationAgent, but with SupplementalInformationController + Helper removed.
 */
public abstract class SubsequentGenerationAgent extends SuperAbstractAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshTransferDestinationExecutor,
                TransferExecutorNxgen,
                PersistentLogin,
                // TODO auth: remove this implements
                ProgressiveAuthAgent {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    protected final Catalog catalog;
    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    protected final SessionStorage sessionStorage;
    protected final Credentials credentials;
    protected final TransactionPaginationHelper transactionPaginationHelper;
    protected final UpdateController updateController;
    protected final MetricRefreshController metricRefreshController;
    // TODO auth: remove helper and controller when refactor is done
    protected final SupplementalInformationFormer supplementalInformationFormer;

    private List<Refresher> refreshers;
    private TransferController transferController;
    private Authenticator authenticator;
    private SessionController sessionController;
    private PaymentController paymentController;

    // Until we can refresh CHECKING and SAVING accounts & transactions separately.
    private boolean hasRefreshedCheckingAccounts = false;
    private boolean hasRefreshedCheckingTransactions = false;

    protected SubsequentGenerationAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);
        this.catalog = context.getCatalog();
        this.persistentStorage = new PersistentStorage();
        this.sessionStorage = new SessionStorage();
        this.credentials = request.getCredentials();
        this.updateController =
                new UpdateController(
                        // TODO: Remove when provider uses MarketCode
                        MarketCode.valueOf(request.getProvider().getMarket()),
                        request.getProvider().getCurrency(),
                        request.getUser());
        this.client =
                new TinkHttpClient(
                        context.getAggregatorInfo(),
                        metricContext.getMetricRegistry(),
                        context.getLogOutputStream(),
                        signatureKeyPair,
                        request.getProvider());
        this.transactionPaginationHelper = new TransactionPaginationHelper(request);
        this.metricRefreshController =
                new MetricRefreshController(
                        metricContext.getMetricRegistry(),
                        request.getProvider(),
                        credentials,
                        request.isManual(),
                        request.getType());
        this.supplementalInformationFormer =
                new SupplementalInformationFormer(request.getProvider());
    }

    // TODO auth: remove the legacy login.
    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        getAuthenticator().authenticate(credentials);
        return true;
    }

    @Override
    public AuthenticationResponse login(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        final LoadedAuthenticationRequest loadedRequest =
                new LoadedAuthenticationRequest(request, credentials);
        return ((ProgressiveAuthenticator) getAuthenticator()).authenticate(loadedRequest);
    }

    @Override
    public void logout() {
        getSessionController().logout();
    }

    @Override
    public boolean isLoggedIn() {
        return getSessionController().isLoggedIn();
    }

    @Override
    public boolean keepAlive() {
        return isLoggedIn();
    }

    @Override
    public void persistLoginSession() {
        getSessionController().store();
    }

    @Override
    public void loadLoginSession() {
        getSessionController().load();
    }

    @Override
    public void clearLoginSession() {
        getSessionController().clear();
    }

    @Override
    public Optional<String> execute(Transfer transfer) {
        Optional<TransferController> transferController = getTransferController();
        TransferExecutionException.throwIf(!transferController.isPresent());

        return transferController.get().execute(transfer);
    }

    @Override
    public void update(Transfer transfer) {
        Optional<TransferController> transferController = getTransferController();
        TransferExecutionException.throwIf(!transferController.isPresent());

        transferController.get().update(transfer);
    }

    @Override
    public void attachHttpFilters(ClientFilterFactory filterFactory) {
        filterFactory.addClientFilter(client.getInternalClient());
    }

    private Authenticator getAuthenticator() {
        if (authenticator == null) {
            authenticator = this.constructAuthenticator();
        }
        return authenticator;
    }

    private List<Refresher> getRefreshControllers() {
        if (refreshers == null) {
            refreshers = new ArrayList<>();
            constructTransactionalAccountRefreshController().ifPresent(refreshers::add);
            constructCreditCardRefreshController().ifPresent(refreshers::add);
            constructInvestmentRefreshController().ifPresent(refreshers::add);
            constructLoanRefreshController().ifPresent(refreshers::add);
            constructTransferDestinationRefreshController().ifPresent(refreshers::add);
        }
        return refreshers;
    }

    @SuppressWarnings("unchecked")
    private <T extends Refresher> List<T> getRefreshControllersOfType(Class<T> cls) {
        return getRefreshControllers().stream()
                .filter(cls::isInstance)
                .map(refresher -> (T) refresher)
                .collect(Collectors.toList());
    }

    private <T extends Refresher> Optional<T> getRefreshController(Class<T> cls) {
        return getRefreshControllersOfType(cls).stream().findFirst();
    }

    private Optional<TransferController> getTransferController() {
        if (transferController == null) {
            transferController = constructTransferController().orElse(null);
        }

        return Optional.ofNullable(transferController);
    }

    private Optional<PaymentController> getPaymentController() {
        if (paymentController == null) {
            paymentController = constructPaymentController().orElse(null);
        }

        return Optional.ofNullable(paymentController);
    }

    private SessionController getSessionController() {
        if (sessionController == null) {
            sessionController =
                    new SessionController(
                            context,
                            client,
                            persistentStorage,
                            sessionStorage,
                            credentials,
                            constructSessionHandler());
        }
        return sessionController;
    }

    protected abstract Authenticator constructAuthenticator();

    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.empty();
    }

    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.empty();
    }

    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.empty();
    }

    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.empty();
    }

    protected Optional<TransferDestinationRefreshController>
            constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    protected abstract SessionHandler constructSessionHandler();

    // transfer and payment executors
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return this.fetchTransactionalAccounts();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return this.fetchTransactionalAccounts();
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return fetchTransactionalAccountsPerType(CreditCardRefreshController.class);
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        Map<Account, AccountFeatures> accounts = new HashMap<>();
        for (AccountRefresher refresher :
                getRefreshControllersOfType(LoanRefreshController.class)) {
            accounts.putAll(refresher.fetchAccounts());
        }

        return new FetchLoanAccountsResponse(accounts);
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        Map<Account, AccountFeatures> accounts = new HashMap<>();
        for (AccountRefresher refresher :
                getRefreshControllersOfType(InvestmentRefreshController.class)) {
            accounts.putAll(refresher.fetchAccounts());
        }

        return new FetchInvestmentAccountsResponse(accounts);
    }

    private FetchAccountsResponse fetchTransactionalAccounts() {
        if (hasRefreshedCheckingAccounts) {
            return new FetchAccountsResponse(Collections.emptyList());
        }
        hasRefreshedCheckingAccounts = true;

        return fetchTransactionalAccountsPerType(TransactionalAccountRefreshController.class);
    }

    private <T extends AccountRefresher> FetchAccountsResponse fetchTransactionalAccountsPerType(
            Class<T> cls) {
        List<Account> accounts = new ArrayList<>();
        for (AccountRefresher refresher : getRefreshControllersOfType(cls)) {
            accounts.addAll(refresher.fetchAccounts().keySet());
        }

        return new FetchAccountsResponse(accounts);
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return this.fetchTransactionalAccountTransactions();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return this.fetchTransactionalAccountTransactions();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return fetchTransactionsPerType(CreditCardRefreshController.class);
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return fetchTransactionsPerType(LoanRefreshController.class);
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        // Todo: implement `TransactionRefresher` in `InvestmentRefreshController`
        return new FetchTransactionsResponse(Collections.emptyMap());
    }

    private FetchTransactionsResponse fetchTransactionalAccountTransactions() {
        if (hasRefreshedCheckingTransactions) {
            return new FetchTransactionsResponse(Collections.emptyMap());
        }
        hasRefreshedCheckingTransactions = true;

        return fetchTransactionsPerType(TransactionalAccountRefreshController.class);
    }

    private <T extends TransactionRefresher> FetchTransactionsResponse fetchTransactionsPerType(
            Class<T> cls) {

        Map<Account, List<Transaction>> transactionsMap = new HashMap<>();

        for (TransactionRefresher refresher : getRefreshControllersOfType(cls)) {
            transactionsMap.putAll(refresher.fetchTransactions());
        }
        return new FetchTransactionsResponse(transactionsMap);
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        TransferDestinationRefreshController destinationRefresher =
                getRefreshController(TransferDestinationRefreshController.class).orElse(null);
        if (destinationRefresher == null) {
            return new FetchTransferDestinationsResponse(Collections.emptyMap());
        }
        Map<Account, List<TransferDestinationPattern>> refreshTransferDestination =
                destinationRefresher.refreshTransferDestinationsFor(accounts);
        return new FetchTransferDestinationsResponse(refreshTransferDestination);
    }

    public Optional<PaymentController> constructPaymentController() {
        return Optional.empty();
    }

    public PaymentResponse createPayment(PaymentRequest paymentRequest) throws PaymentException {
        return getPaymentController()
                .orElseThrow(
                        () -> new NotImplementedException("PaymentController not implemented."))
                .create(paymentRequest);
    }

    public PaymentResponse fetchPayment(PaymentRequest paymentRequest) throws PaymentException {
        return getPaymentController()
                .orElseThrow(
                        () -> new NotImplementedException("PaymentController not implemented."))
                .fetch(paymentRequest);
    }

    public PaymentMultiStepResponse signPayment(PaymentMultiStepRequest paymentRequest)
            throws PaymentException {
        return getPaymentController()
                .orElseThrow(
                        () -> new NotImplementedException("PaymentController not implemented."))
                .sign(paymentRequest);
    }

    public PaymentResponse cancelPayment(PaymentRequest paymentRequest) {
        return getPaymentController()
                .orElseThrow(
                        () -> new NotImplementedException("PaymentController not implemented."))
                .cancel(paymentRequest);
    }

    public PaymentListResponse fetchMultiplePayments(PaymentListRequest paymentListRequest)
            throws PaymentException {
        return getPaymentController()
                .orElseThrow(
                        () -> new NotImplementedException("PaymentController not implemented."))
                .fetchMultiple(paymentListRequest);
    }
}
