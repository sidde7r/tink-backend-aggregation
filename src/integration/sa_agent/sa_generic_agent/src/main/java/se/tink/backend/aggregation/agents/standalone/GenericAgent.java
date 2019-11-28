package se.tink.backend.aggregation.agents.standalone;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.ProgressiveAuthAgent;
import se.tink.backend.aggregation.agents.standalone.grpc.AuthenticationService;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class GenericAgent implements Agent, ProgressiveAuthAgent {

    private GenericAgentConfiguration genericAgentConfiguration;
    private AgentsServiceConfiguration agentsServiceConfiguration;
    private final ManagedChannel channel;
    private final AuthenticationService authenticationService;

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
        authenticationService = new AuthenticationService(channel);
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
}
