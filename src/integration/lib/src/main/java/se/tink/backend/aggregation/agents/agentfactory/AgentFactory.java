package se.tink.backend.aggregation.agents.agentfactory;

import com.google.common.base.Objects;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Set;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AgentFactory {
    private final AgentsServiceConfiguration configuration;
    private final AgentModuleFactory moduleLoader;

    @Inject
    public AgentFactory(AgentModuleFactory moduleLoader, AgentsServiceConfiguration configuration) {
        this.moduleLoader = moduleLoader;
        this.configuration = configuration;
    }

    /**
     * Creates agent from the className specified in the provider contained in the {@code request}
     * using one of the supported constructor signatures.
     *
     * @param request Request that the agent will serve.
     * @param context Context to execute the agent within.
     * @return An agent.
     * @throws ReflectiveOperationException If appropriate constructor or module can't be found.
     */
    public Agent create(CredentialsRequest request, AgentContext context)
            throws ReflectiveOperationException {

        Class<? extends Agent> agentClass =
                getAgentClass(request.getCredentials(), request.getProvider());

        if (hasInjectAnnotatedConstructor(agentClass)) {

            return create(
                    agentClass, moduleLoader.getAgentModules(request, context, configuration));
        } else {

            // Backwards compatibility with non guice enabled agents.
            return create(agentClass, request, context);
        }
    }

    /**
     * Creates an agent using guice dependency injection using dependencies specified in {@code
     * modules}.
     *
     * @param agentClass Agent to be instantiated.
     * @param modules Modules to be bound by guice.
     * @return An agent constructed using guice dependency injection constructor.
     */
    private Agent create(Class<? extends Agent> agentClass, Set<? extends Module> modules) {

        final Injector injector = Guice.createInjector(modules);
        final Agent agent = injector.getInstance(agentClass);
        agent.setConfiguration(configuration);

        return agent;
    }

    /**
     * @deprecated Agents should implement guice injection constructor.
     * @return An agent constructed using its {@link AgentComponentProvider } constructor or, if it
     *     doesn't exist, its {@link AgentsServiceConfiguration } constructor or, if it doesn't
     *     exist, its {@link SignatureKeyPair } constructor.
     */
    @Deprecated
    public Agent create(
            Class<? extends Agent> agentClass, CredentialsRequest request, AgentContext context)
            throws ReflectiveOperationException {

        final Class<?>[] agentsServiceConfigurationParameterList = {
            CredentialsRequest.class, AgentContext.class, AgentsServiceConfiguration.class
        };

        final boolean hasAgentsServiceConfigurationConstructor =
                Arrays.stream(agentClass.getConstructors())
                        .anyMatch(
                                c ->
                                        Arrays.equals(
                                                c.getParameterTypes(),
                                                agentsServiceConfigurationParameterList));

        final Agent agent;
        context.setConfiguration(configuration);

        if (hasAgentsServiceConfigurationConstructor) {
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

    private static Class<? extends Agent> getAgentClass(Credentials credentials, Provider provider)
            throws ReflectiveOperationException {
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

    static boolean hasInjectAnnotatedConstructor(Class cls) {

        Constructor[] constructors = cls.getDeclaredConstructors();
        for (Constructor constructor : constructors) {
            if (constructor.getAnnotation(Inject.class) != null) {
                return true;
            }
        }

        return false;
    }
}
