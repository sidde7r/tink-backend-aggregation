package se.tink.backend.aggregation.agents.standalone;

import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.ProgressiveAuthAgent;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.sa.agent.facade.AuthenticationFacade;

public class GenericAgent implements Agent, ProgressiveAuthAgent {

    private AgentsServiceConfiguration agentsServiceConfiguration;

    public GenericAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {}

    @Override
    public SteppableAuthenticationResponse login(SteppableAuthenticationRequest request)
            throws Exception {
        return AuthenticationFacade.getInstance().login(request);
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
