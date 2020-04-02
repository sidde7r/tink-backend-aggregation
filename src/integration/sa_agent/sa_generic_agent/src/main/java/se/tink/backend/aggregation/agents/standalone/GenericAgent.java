package se.tink.backend.aggregation.agents.standalone;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.agents.standalone.caller.GetConsentStatusCaller;
import se.tink.backend.aggregation.agents.standalone.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.standalone.grpc.AuthenticationService;
import se.tink.backend.aggregation.agents.standalone.grpc.CheckingService;
import se.tink.backend.aggregation.agents.standalone.mapper.MappingContextKeys;
import se.tink.backend.aggregation.agents.standalone.mapper.factory.MappersController;
import se.tink.backend.aggregation.agents.standalone.mapper.providers.CommonExternalParametersProvider;
import se.tink.backend.aggregation.agents.standalone.mapper.providers.impl.MockCommonExternalParametersProvider;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.constants.MarketCode;
import se.tink.backend.aggregation.nxgen.controllers.authentication.*;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.ThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.UpdateController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.model.auth.GetConsentStatusRequest;

public class GenericAgent implements Agent, ProgressiveAuthAgent, RefreshCheckingAccountsExecutor {

    private static final Logger logger = LoggerFactory.getLogger(GenericAgent.class);

    private static final long SUPPLEMENTAL_WAIT_REQUEST_MINUTES = 10L;
    private static final long RETRYER_SLEEP_TIME = 10L; // in seconds
    private static final int RETRYER_RETRY_ATTEMPTS = 10;

    private final PersistentStorage persistentStorage;
    private GenericAgentConfiguration genericAgentConfiguration;
    private CheckingService checkingService;
    private AgentsServiceConfiguration agentsServiceConfiguration;
    private final ManagedChannel channel;
    private final AuthenticationService authenticationService;
    private final CredentialsRequest credentialsRequest;
    private final StrongAuthenticationState strongAuthenticationState;
    private LinkedList<? extends AuthenticationStep> authenticationSteps;
    private final MappersController mappersController;
    private final Retryer<ConsentStatus> consentStatusRetryer;
    protected final MetricRefreshController metricRefreshController;
    protected final TransactionPaginationHelper transactionPaginationHelper;

    private final AgentContext context;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public GenericAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        this.context = context;
        genericAgentConfiguration =
                context.getAgentConfigurationController()
                        .getAgentConfiguration(GenericAgentConfiguration.class);
        channel =
                ManagedChannelBuilder.forAddress(
                                genericAgentConfiguration.getGrpcHost(),
                                genericAgentConfiguration.getGrpcPort())
                        .usePlaintext()
                        .build();

        this.persistentStorage = new PersistentStorage();
        this.strongAuthenticationState = new StrongAuthenticationState(request.getAppUriId());
        credentialsRequest = request;
        CommonExternalParametersProvider commonExternalParametersProvider =
                new MockCommonExternalParametersProvider();
        mappersController =
                MappersController.newInstance(commonExternalParametersProvider, credentialsRequest);

        consentStatusRetryer =
                RetryerBuilder.<ConsentStatus>newBuilder()
                        .retryIfResult(status -> status != null && !status.isFinalStatus())
                        .withWaitStrategy(
                                WaitStrategies.fixedWait(RETRYER_SLEEP_TIME, TimeUnit.SECONDS))
                        .withStopStrategy(StopStrategies.stopAfterAttempt(RETRYER_RETRY_ATTEMPTS))
                        .build();

        authenticationService =
                new AuthenticationService(
                        channel,
                        strongAuthenticationState,
                        genericAgentConfiguration,
                        mappersController);
        checkingService =
                new CheckingService(
                        channel,
                        strongAuthenticationState,
                        genericAgentConfiguration,
                        mappersController,
                        persistentStorage);

        this.metricRefreshController =
                new MetricRefreshController(
                        context.getMetricRegistry(),
                        request.getProvider(),
                        request.getCredentials(),
                        request.isManual(),
                        request.getType());
        this.transactionPaginationHelper = new TransactionPaginationHelper(request);
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    @Override
    public SteppableAuthenticationResponse login(SteppableAuthenticationRequest request)
            throws Exception {
        if (request.getPayload() != null
                && request.getPayload().getCallbackData() != null
                && !request.getPayload().getCallbackData().isEmpty()) {
            processCallbackData(request.getPayload().getCallbackData());
            return SteppableAuthenticationResponse.finalResponse();
        }

        // TODO: when ProgressiveLoginExecutor will be ready to properly deal with
        //  ThirdPartyAppAuthenticationStep
        //  remove authenticationSteps and return only one ThirdPartyAppAuthenticationStep (first
        //  from current list
        if (authenticationSteps == null || authenticationSteps.isEmpty()) {
            authenticationSteps = buildAuthenticationSteps(request);
        }

        AuthenticationStep step = authenticationSteps.poll();
        return SteppableAuthenticationResponse.intermediateResponse(
                step.getIdentifier(),
                step.execute(request.getPayload()).getSupplementInformationRequester().get());
    }

    private SupplementalWaitRequest buildSupplementalWaitRequest() {
        return new SupplementalWaitRequest(
                strongAuthenticationState.getSupplementalKey(),
                SUPPLEMENTAL_WAIT_REQUEST_MINUTES,
                TimeUnit.MINUTES);
    }

    private AuthenticationStepResponse processCallbackData(final Map<String, String> callbackData) {
        try {
            String consentId =
                    persistentStorage.get(GenericAgentConstants.PersistentStorageKey.CONSENT_ID);
            MappingContext mappingContext =
                    MappingContext.newInstance().put(MappingContextKeys.CONSENT_ID, consentId);
            GetConsentStatusRequest getConsentStatusRequest =
                    mappersController
                            .getConsentStatusRequestMapper()
                            .map(consentId, mappingContext);
            GetConsentStatusCaller getConsentStatusCaller =
                    new GetConsentStatusCaller(authenticationService, getConsentStatusRequest);

            ConsentStatus consentStatus = consentStatusRetryer.call(getConsentStatusCaller);

            logger.debug("fetched consentStatus {}", consentStatus);
        } catch (ExecutionException | RetryException e) {
            logger.warn("Authorization failed, consents status is not accepted.", e);
        }
        return AuthenticationStepResponse.executeNextStep();
    }

    private LinkedList<? extends AuthenticationStep> buildAuthenticationSteps(
            SteppableAuthenticationRequest request) {
        LinkedList<AuthenticationStep> linkedList = new LinkedList<>();

        ThirdPartyAppAuthenticationStep openThirdPartyApp =
                new ThirdPartyAppAuthenticationStep(
                        "sibsThirdPartyAuthStep",
                        authenticationService.login(request, persistentStorage),
                        buildSupplementalWaitRequest(),
                        this::processCallbackData);

        ThirdPartyAppAuthenticationStep waitForResponse =
                new ThirdPartyAppAuthenticationStep(
                        "sibsThirdPartyWaitAuthStep",
                        null,
                        buildSupplementalWaitRequest(),
                        this::processCallbackData);

        linkedList.add(openThirdPartyApp);
        linkedList.add(waitForResponse);

        return linkedList;
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        this.agentsServiceConfiguration = configuration;
    }

    @Override
    public Class<? extends Agent> getAgentClass() {
        return GenericAgent.class;
    }

    @Override
    public boolean login() throws Exception {
        throw new RuntimeException("This is stateless agent. Method will not be implemented");
    }

    @Override
    public void logout() throws Exception {
        throw new RuntimeException("This is stateless agent. Method will not be implemented");
    }

    @Override
    public void close() {
        throw new RuntimeException("This is stateless agent. Method will not be implemented");
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        final GASibsTransactionalAccountAccountFetcher accountFetcher =
                new GASibsTransactionalAccountAccountFetcher(checkingService);
        final GATransactionalAccountTransactionFetcher transactionFetcher =
                new GATransactionalAccountTransactionFetcher(checkingService);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                new UpdateController(
                        // TODO: Remove when provider uses MarketCode
                        MarketCode.valueOf(credentialsRequest.getProvider().getMarket()),
                        credentialsRequest.getProvider().getCurrency(),
                        credentialsRequest.getUser()),
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }
}
