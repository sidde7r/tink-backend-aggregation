package se.tink.backend.aggregation.agents.standalone;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.ProgressiveAuthAgent;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.standalone.grpc.AuthenticationService;
import se.tink.backend.aggregation.agents.standalone.grpc.CheckingService;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class GenericAgent implements Agent, ProgressiveAuthAgent, RefreshCheckingAccountsExecutor {

    private GenericAgentConfiguration genericAgentConfiguration;
    private CheckingService checkingService;
    private AgentsServiceConfiguration agentsServiceConfiguration;
    private final ManagedChannel channel;
    private final AuthenticationService authenticationService;
    private final CredentialsRequest credentialsRequest;
    private final StrongAuthenticationState strongAuthenticationState;

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

        this.strongAuthenticationState = new StrongAuthenticationState(request.getAppUriId());

        authenticationService =
                new AuthenticationService(
                        channel, strongAuthenticationState, genericAgentConfiguration);
        checkingService =
                new CheckingService(channel, strongAuthenticationState, genericAgentConfiguration);
        credentialsRequest = request;
    }

    @Override
    public SteppableAuthenticationResponse login(SteppableAuthenticationRequest request)
            throws Exception {
        return authenticationService.login(request);
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
