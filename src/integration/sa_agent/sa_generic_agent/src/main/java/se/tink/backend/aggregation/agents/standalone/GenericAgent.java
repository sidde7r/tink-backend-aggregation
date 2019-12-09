package se.tink.backend.aggregation.agents.standalone;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.ProgressiveAuthAgent;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.standalone.grpc.AuthenticationService;
import se.tink.backend.aggregation.agents.standalone.grpc.CheckingService;
import se.tink.backend.aggregation.agents.standalone.mapper.factory.MappersController;
import se.tink.backend.aggregation.agents.standalone.mapper.providers.CommonExternalParametersProvider;
import se.tink.backend.aggregation.agents.standalone.mapper.providers.impl.MockCommonExternalParametersProvider;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.ThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class GenericAgent implements Agent, ProgressiveAuthAgent, RefreshCheckingAccountsExecutor {

    private static final long SUPPLEMENTAL_WAIT_REQUEST_MINUTES = 10L;

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

    public GenericAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
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
                        mappersController);
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
                step.getIdentifier(), step.execute(request.getPayload()).get());
    }

    private SupplementalWaitRequest buildSupplementalWaitRequest() {
        return new SupplementalWaitRequest(
                strongAuthenticationState.getSupplementalKey(),
                SUPPLEMENTAL_WAIT_REQUEST_MINUTES,
                TimeUnit.MINUTES);
    }

    private void processCallbackData(final Map<String, String> callbackData) {
        String state = callbackData.get("state");
    }

    private LinkedList<? extends AuthenticationStep> buildAuthenticationSteps(
            SteppableAuthenticationRequest request) {
        LinkedList<AuthenticationStep> linkedList = new LinkedList<>();

        ThirdPartyAppAuthenticationStep openThirdPartyApp =
                new ThirdPartyAppAuthenticationStep(
                        authenticationService.login(request),
                        buildSupplementalWaitRequest(),
                        this::processCallbackData);

        ThirdPartyAppAuthenticationStep waitForResponse =
                new ThirdPartyAppAuthenticationStep(
                        null, buildSupplementalWaitRequest(), this::processCallbackData);

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
        return checkingService.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return checkingService.fetchCheckingTransactions();
    }
}
