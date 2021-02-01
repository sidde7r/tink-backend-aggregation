package se.tink.backend.aggregation.nxgen.agents;

import java.security.Security;
import java.util.Optional;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.CreateBeneficiaryControllerable;
import se.tink.backend.aggregation.agents.PaymentControllerable;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.SuperAbstractAgent;
import se.tink.backend.aggregation.agents.TransferExecutorNxgen;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.UpdateController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.factory.ClientFilterFactory;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.transfer.rpc.Transfer;

/**
 * Same as the old NextGenerationAgent, but with SupplementalInformationController + Helper and the
 * imposing authenticator removed.
 */
public abstract class SubsequentGenerationAgent<Auth> extends SuperAbstractAgent
        implements TransferExecutorNxgen,
                PaymentControllerable,
                CreateBeneficiaryControllerable,
                PersistentLogin {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String DEFAULT_USER_IP = "127.0.0.1";

    protected final Catalog catalog;
    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    protected final SessionStorage sessionStorage;
    protected final Credentials credentials;
    protected final Provider provider;
    protected final String userIp;
    protected final TransactionPaginationHelper transactionPaginationHelper;
    protected final UpdateController updateController;
    protected final MetricRefreshController metricRefreshController;
    protected final String appId;
    protected final StrongAuthenticationState strongAuthenticationState;

    // The SupplementalInforamtionController is the lowest level of API an agent should use
    protected final SupplementalInformationController supplementalInformationController;
    // FIXME: Remove or consolidate different types of helpers. There are now too many variants
    // FIXME: that rather causes confusion (as opposed to helping).
    protected final SupplementalInformationHelper supplementalInformationHelper;
    protected final SupplementalInformationFormer supplementalInformationFormer;

    protected PaymentController paymentController;
    private TransferController transferController;
    private SessionController sessionController;
    private Optional<CreateBeneficiaryController> createBeneficiaryController = Optional.empty();

    protected SubsequentGenerationAgent(final AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.catalog = context.getCatalog();
        this.persistentStorage = new PersistentStorage();
        this.sessionStorage = new SessionStorage();
        context.getLogMasker()
                .addSensitiveValuesSetObservable(persistentStorage.getSensitiveValuesObservable());
        context.getLogMasker()
                .addSensitiveValuesSetObservable(sessionStorage.getSensitiveValuesObservable());
        this.credentials = request.getCredentials();
        this.provider = request.getProvider();
        this.updateController = new UpdateController(provider, request.getUser());

        this.client = componentProvider.getTinkHttpClient();
        if (context.getAgentConfigurationController().isOpenBankingAgent()) {
            client.disableSignatureRequestHeader();
        }
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
        this.supplementalInformationController = componentProvider.getSupplementalInformationController();
        this.supplementalInformationHelper = componentProvider.getSupplementalInformationHelper();
        this.appId = context.getAppId();
        this.strongAuthenticationState = new StrongAuthenticationState(request.getState());

        this.userIp = getOriginatingUserIpOrDefault();
    }

    // This helper `userIp` field is meant to be used by agents that agree to use a default value in
    // case of more "true" value of originatingUserIp missing. It can happen in rare cases, even for
    // manual refreshes.
    private String getOriginatingUserIpOrDefault() {
        return Optional.ofNullable(request.getOriginatingUserIp()).orElse(DEFAULT_USER_IP);
    }

    protected EidasIdentity getEidasIdentity() {
        return new EidasIdentity(context.getClusterId(), context.getAppId(), getAgentClass());
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasIdentity(getEidasIdentity());
        client.setDebugOutput(configuration.getTestConfiguration().isDebugOutputEnabled());
        client.setEidasProxyConfiguration(configuration.getEidasProxy());
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
    public void attachHttpFilters(ClientFilterFactory filterFactory) {
        filterFactory.addClientFilter(client.getInternalClient());
    }

    private Optional<TransferController> getTransferController() {
        if (transferController == null) {
            transferController = constructTransferController().orElse(null);
        }

        return Optional.ofNullable(transferController);
    }

    @Override
    public Optional<PaymentController> getPaymentController() {
        if (paymentController == null) {
            paymentController = constructPaymentController().orElse(null);
        }

        return Optional.ofNullable(paymentController);
    }

    @Override
    public Optional<CreateBeneficiaryController> getCreateBeneficiaryController() {
        if (!createBeneficiaryController.isPresent()) {
            createBeneficiaryController = constructCreateBeneficiaryController();
        }
        return createBeneficiaryController;
    }

    private SessionController getSessionController() {
        if (sessionController == null) {
            sessionController =
                    new SessionController(
                            client,
                            persistentStorage,
                            sessionStorage,
                            credentials,
                            constructSessionHandler());
        }
        return sessionController;
    }

    protected abstract SessionHandler constructSessionHandler();

    // transfer and payment executors
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    public Optional<PaymentController> constructPaymentController() {
        return Optional.empty();
    }

    public Optional<CreateBeneficiaryController> constructCreateBeneficiaryController() {
        return Optional.empty();
    }

    public abstract Auth getAuthenticator();

    protected PersistentStorage getPersistentStorage() {
        return persistentStorage;
    }
}
