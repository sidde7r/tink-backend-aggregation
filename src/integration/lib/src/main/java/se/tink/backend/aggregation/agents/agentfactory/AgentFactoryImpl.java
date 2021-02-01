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
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class AgentFactoryImpl implements AgentFactory {
    private static final String METRIC_LABEL_AGENT = "agent";
    private static final String METRIC_LABEL_TYPE = "type";
    private static final MetricId INSTANTIATION_METRIC_ID =
            MetricId.newId("agent_instantiation_variant");
    private final AgentsServiceConfiguration configuration;
    private final AgentModuleFactory moduleLoader;
    private final MetricRegistry metricRegistry;

    @Inject
    private AgentFactoryImpl(
            AgentModuleFactory moduleLoader,
            AgentsServiceConfiguration configuration,
            MetricRegistry metricRegistry) {
        this.moduleLoader = moduleLoader;
        this.configuration = configuration;
        this.metricRegistry = metricRegistry;
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
    @Override
    public Agent create(CredentialsRequest request, AgentContext context)
            throws ReflectiveOperationException {

        Class<? extends Agent> agentClass =
                getAgentClass(request.getCredentials(), request.getProvider());

        if (AgentFactoryUtils.hasInjectAnnotatedConstructor(agentClass)) {
            metricRegistry
                    .meter(
                            INSTANTIATION_METRIC_ID
                                    .label(METRIC_LABEL_TYPE, "guice-injection")
                                    .label(METRIC_LABEL_AGENT, agentClass.getSimpleName()))
                    .inc();
            return create(
                    agentClass,
                    moduleLoader.getAgentModules(agentClass, request, context, configuration));
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
        if (hasAgentsServiceConfigurationConstructor) {
            metricRegistry
                    .meter(
                            INSTANTIATION_METRIC_ID
                                    .label(METRIC_LABEL_TYPE, "ctor-agent-config")
                                    .label(METRIC_LABEL_AGENT, agentClass.getSimpleName()))
                    .inc();
            Constructor<?> agentConstructor =
                    agentClass.getConstructor(
                            CredentialsRequest.class,
                            AgentContext.class,
                            AgentsServiceConfiguration.class);

            agent = (Agent) agentConstructor.newInstance(request, context, configuration);
        } else {
            metricRegistry
                    .meter(
                            INSTANTIATION_METRIC_ID
                                    .label(METRIC_LABEL_TYPE, "ctor-signature-key-pair")
                                    .label(METRIC_LABEL_AGENT, agentClass.getSimpleName()))
                    .inc();
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
}
