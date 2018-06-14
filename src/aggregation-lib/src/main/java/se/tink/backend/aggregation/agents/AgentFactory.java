package se.tink.backend.aggregation.agents;

import com.google.common.base.Objects;
import java.lang.reflect.Constructor;
import se.tink.backend.aggregation.agents.demo.DemoAgent;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.workers.AgentWorker;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.aggregation.rpc.Credentials;

public class AgentFactory {
    private ServiceConfiguration configuration;

    public AgentFactory(ServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Agent> getAgentClass(Provider provider) throws ClassNotFoundException {
        return (Class<? extends Agent>) Class.forName(AgentWorker.DEFAULT_AGENT_PACKAGE_CLASS_PREFIX + "."
                + provider.getClassName());
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Agent> getAgentClass(Credentials credentials, Provider provider) throws Exception {
        Class<? extends Agent> agentClass;

        // Check if this is demo account.

        if (credentials.isDemoCredentials() && !Objects.equal(credentials.getType(), CredentialsTypes.FRAUD)) {
            agentClass = DemoAgent.class;
            credentials.setPassword("demo");
        } else {
            agentClass = getAgentClass(provider);
        }

        return agentClass;
    }

    public Agent create(CredentialsRequest request, AgentContext context) throws Exception {
        Class<? extends Agent> agentClass = getAgentClass(request.getCredentials(), request.getProvider());
        
        return create(agentClass, request, context);
    }

    public Agent create(Class<? extends Agent> agentClass, CredentialsRequest request, AgentContext context) throws Exception {
        Constructor<?> agentConstructor = agentClass.getConstructor(CredentialsRequest.class, AgentContext.class);
        
        Agent agent = (Agent) agentConstructor.newInstance(request, context);
        agent.setConfiguration(configuration);

        return agent;
    }
}
