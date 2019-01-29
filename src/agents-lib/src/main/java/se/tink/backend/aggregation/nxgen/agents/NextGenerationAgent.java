package se.tink.backend.aggregation.nxgen.agents;

import com.google.common.base.Strings;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutorNxgen;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.constants.MarketCode;
import se.tink.backend.aggregation.nxgen.http.filter.ClientFilterFactory;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountRefresher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.Refresher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.TransactionRefresher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.UpdateController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.configuration.IntegrationsConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.i18n.Catalog;

public abstract class NextGenerationAgent extends AbstractAgent implements RefreshableItemExecutor,
        TransferExecutorNxgen, PersistentLogin {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    protected final Catalog catalog;
    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    protected final SessionStorage sessionStorage;
    protected final Credentials credentials;
    protected final TransactionPaginationHelper transactionPaginationHelper;
    protected final SupplementalInformationController supplementalInformationController;
    protected final UpdateController updateController;
    protected final MetricRefreshController metricRefreshController;
    protected final SupplementalInformationHelper supplementalInformationHelper;

    private List<Refresher> refreshers;
    private TransferController transferController;
    private Authenticator authenticator;
    private SessionController sessionController;

    // Until we can refresh CHECKING and SAVING accounts & transactions separately.
    private boolean hasRefreshedCheckingAccounts = false;
    private boolean hasRefreshedCheckingTransactions = false;

    protected NextGenerationAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);
        this.catalog = context.getCatalog();
        this.persistentStorage = new PersistentStorage();
        this.sessionStorage = new SessionStorage();
        this.credentials = request.getCredentials();
        this.updateController = new UpdateController(context,
                // TODO: Remove when provider uses MarketCode
                MarketCode.valueOf(request.getProvider().getMarket()),
                request.getProvider().getCurrency(), request.getUser());
        this.client = new TinkHttpClient(context, credentials, signatureKeyPair);
        this.transactionPaginationHelper = new TransactionPaginationHelper(request);
        this.supplementalInformationController = new SupplementalInformationController(context, credentials);
        this.metricRefreshController = new MetricRefreshController(
                context.getMetricRegistry(),
                request.getProvider(),
                credentials,
                request.isManual(),
                request.getType());
        this.supplementalInformationHelper = new SupplementalInformationHelper(
                request.getProvider(),
                supplementalInformationController);
        configureHttpClient(client);
    }

    protected void setMultiIpGateway(IntegrationsConfiguration integrationsConfiguration) {
        if (Objects.isNull(integrationsConfiguration)) {
            log.warn("Proxy-setup: integrationsConfiguration is null.");
            return;
        }

        String proxyUri = integrationsConfiguration.getProxyUri();
        if (Strings.isNullOrEmpty(proxyUri)) {
            log.warn("Proxy-setup: proxyUri is null or empty.");
            return;
        }

        // The username (userId) and password (credentialsId) are used as a key in the proxy
        // to select the public IP address in the proxy.
        // The values themselves does not matter, as long as the same credentialsId always
        // is routed from the same public IP.
        client.setProductionProxy(proxyUri, credentials.getUserId(), credentials.getId());
        log.info("Proxy-setup: successfully attached proxy.");
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        getAuthenticator().authenticate(credentials);
        return true;
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
    public void refresh(RefreshableItem item) {
        switch (item) {
        case EINVOICES:
            refreshEInvoices();
            break;

        case TRANSFER_DESTINATIONS:
            refreshTransferDestinations();
            break;

        // We cannot at this layer distinguish between CHECKING and SAVINGS accounts. Future improvement.
        case CHECKING_ACCOUNTS:
        case SAVING_ACCOUNTS:
            if (hasRefreshedCheckingAccounts) {
                break;
            }
            hasRefreshedCheckingAccounts = true;

            refreshAccountsPerType(TransactionalAccountRefreshController.class);
            break;

        // We cannot at this layer distinguish between CHECKING and SAVINGS transactions. Future improvement.
        case CHECKING_TRANSACTIONS:
        case SAVING_TRANSACTIONS:
            if (hasRefreshedCheckingTransactions) {
                break;
            }
            hasRefreshedCheckingTransactions = true;

            refreshTransactionsPerType(TransactionalAccountRefreshController.class);
            break;

        case CREDITCARD_ACCOUNTS:
            refreshAccountsPerType(CreditCardRefreshController.class);
            break;

        case CREDITCARD_TRANSACTIONS:
            refreshTransactionsPerType(CreditCardRefreshController.class);
            break;

        case LOAN_ACCOUNTS:
            refreshAccountsPerType(LoanRefreshController.class);
            break;

        case LOAN_TRANSACTIONS:
            refreshTransactionsPerType(LoanRefreshController.class);
            break;

        case INVESTMENT_ACCOUNTS:
            refreshAccountsPerType(InvestmentRefreshController.class);
            break;

        case INVESTMENT_TRANSACTIONS:
            // Todo: implement `TransactionRefresher` in `InvestmentRefreshController`
            break;
        }
    }

    private <T extends AccountRefresher> void refreshAccountsPerType(Class<T> cls) {
        getRefreshControllersOfType(cls).forEach(AccountRefresher::refreshAccounts);
    }

    private <T extends TransactionRefresher> void refreshTransactionsPerType(Class<T> cls) {
        getRefreshControllersOfType(cls).forEach(TransactionRefresher::refreshTransactions);
    }

    private void refreshTransferDestinations() {
        getRefreshController(TransferDestinationRefreshController.class)
                .ifPresent(destinationRefresher ->
                        destinationRefresher.refreshTransferDestinationsFor(updateController.getAccounts())
                );
    }

    private void refreshEInvoices() {
        getRefreshController(EInvoiceRefreshController.class)
                .ifPresent(EInvoiceRefreshController::refreshEInvoices);
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
            constructEInvoiceRefreshController().ifPresent(refreshers::add);
            constructTransferDestinationRefreshController().ifPresent(refreshers::add);
        }
        return refreshers;
    }

    @SuppressWarnings("unchecked")
    private <T extends Refresher> List<T> getRefreshControllersOfType(Class<T> cls) {
        return getRefreshControllers().stream()
                .filter(cls::isInstance)
                .map(refresher ->  (T) refresher)
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

    private SessionController getSessionController() {
        if (sessionController == null) {
            sessionController = new SessionController(context, client, persistentStorage, sessionStorage, credentials,
                    constructSessionHandler());
        }
        return sessionController;
    }

    protected abstract void configureHttpClient(TinkHttpClient client);
    protected abstract Authenticator constructAuthenticator();

    protected abstract Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController();
    protected abstract Optional<CreditCardRefreshController> constructCreditCardRefreshController();
    protected abstract Optional<InvestmentRefreshController> constructInvestmentRefreshController();
    protected abstract Optional<LoanRefreshController> constructLoanRefreshController();
    protected abstract Optional<EInvoiceRefreshController> constructEInvoiceRefreshController();
    protected abstract Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController();

    protected abstract SessionHandler constructSessionHandler();

    // transfer and payment executors
    protected abstract Optional<TransferController> constructTransferController();
}
