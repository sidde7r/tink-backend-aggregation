package se.tink.backend.aggregation.agents;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AgentFactory {
    private AgentsServiceConfiguration configuration;

    @Inject
    public AgentFactory(AgentsServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Agent> getAgentClass(Credentials credentials, Provider provider)
            throws Exception {
        Class<? extends Agent> agentClass;

        // Check if this is demo account.

        if (credentials.isDemoCredentials()
                && !Objects.equal(credentials.getType(), CredentialsTypes.FRAUD)) {
            agentClass = AgentClassFactory.getAgentClass("demo.DemoAgent");
            credentials.setPassword("demo");
        } else {
            agentClass = AgentClassFactory.getAgentClass(provider);
        }

        return agentClass;
    }

    public Agent create(CredentialsRequest request, AgentContext context) throws Exception {
        Class<? extends Agent> agentClass =
                getAgentClass(request.getCredentials(), request.getProvider());

        return create(agentClass, request, context);
    }

    /**
     * @return An agent constructed using its {@link AgentsServiceConfiguration } constructor or, if
     *     it doesn't exist, its {@link SignatureKeyPair } constructor.
     */
    public Agent create(
            Class<? extends Agent> agentClass, CredentialsRequest request, AgentContext context)
            throws Exception {

        final Class<?>[] altParameterList = {
            CredentialsRequest.class, AgentContext.class, AgentsServiceConfiguration.class
        };

        final boolean hasAlternativeConstructor =
                Arrays.stream(agentClass.getConstructors())
                        .anyMatch(c -> Arrays.equals(c.getParameterTypes(), altParameterList));

        final Agent agent;

        if (hasAlternativeConstructor) {
            Constructor<?> agentConstructor =
                    agentClass.getConstructor(
                            CredentialsRequest.class,
                            AgentContext.class,
                            AgentsServiceConfiguration.class);

            agent = (Agent) agentConstructor.newInstance(request, context, configuration);
        } else {
            Constructor<?> agentConstructor =
                    agentClass.getConstructor(
                            CredentialsRequest.class, AgentContext.class, SignatureKeyPair.class);

            SignatureKeyPair signatureKeyPair = configuration.getSignatureKeyPair();

            agent = (Agent) agentConstructor.newInstance(request, context, signatureKeyPair);
        }

        agent.setConfiguration(configuration);

        return agent;
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Agent> getAgentClass(String className) throws Exception {
        return AgentClassFactory.getAgentClass(className);
    }

    public Agent createForIntegration(
            String className, CredentialsRequest request, AgentContext context) throws Exception {
        Class<? extends Agent> agentClass = getAgentClass(className);
        Constructor<?> agentConstructor =
                agentClass.getConstructor(
                        CredentialsRequest.class, AgentContext.class, SignatureKeyPair.class);

        Agent agent =
                (Agent)
                        agentConstructor.newInstance(
                                request, context, configuration.getSignatureKeyPair());
        agent.setConfiguration(configuration);

        return agent;
    }
}
