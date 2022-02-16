package se.tink.backend.aggregation.nxgen.controllers.payment.validation.impl;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;

@Ignore
public class ValidationTestAgent implements Agent {

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends Agent> getAgentClass() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean login() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void logout() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }
}
