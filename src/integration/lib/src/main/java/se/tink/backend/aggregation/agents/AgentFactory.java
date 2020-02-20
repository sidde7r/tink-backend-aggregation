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
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.factory.AgentContextProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.factory.SupplementalInformationProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.factory.TinkHttpClientProviderFactory;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AgentFactory {
    private AgentsServiceConfiguration configuration;
    private final TinkHttpClientProviderFactory tinkHttpClientProviderFactory;
    private final SupplementalInformationProviderFactory supplementalInformationProviderFactory;
    private final AgentContextProviderFactory agentContextProviderFactory;

    @Inject
    public AgentFactory(
            AgentsServiceConfiguration configuration,
            TinkHttpClientProviderFactory tinkHttpClientProviderFactory,
            SupplementalInformationProviderFactory supplementalInformationProviderFactory,
            AgentContextProviderFactory agentContextProviderFactory) {
        this.configuration = configuration;
        this.tinkHttpClientProviderFactory = tinkHttpClientProviderFactory;
        this.supplementalInformationProviderFactory = supplementalInformationProviderFactory;
        this.agentContextProviderFactory = agentContextProviderFactory;
    }

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
     * @return An agent constructed using its {@link AgentComponentProvider } constructor or, if it
     *     doesn't exist, its {@link AgentsServiceConfiguration } constructor or, if it doesn't
     *     exist, its {@link SignatureKeyPair } constructor.
     */
    public Agent create(
            Class<? extends Agent> agentClass, CredentialsRequest request, AgentContext context)
            throws Exception {

        final Class<?>[] strategyParameterList = {AgentComponentProvider.class};

        final Class<?>[] agentsServiceConfigurationParameterList = {
            CredentialsRequest.class, AgentContext.class, AgentsServiceConfiguration.class
        };

        final boolean hasStrategyConstructor =
                Arrays.stream(agentClass.getConstructors())
                        .anyMatch(c -> Arrays.equals(c.getParameterTypes(), strategyParameterList));

        final boolean hasAgentsServiceConfigurationConstructor =
                Arrays.stream(agentClass.getConstructors())
                        .anyMatch(
                                c ->
                                        Arrays.equals(
                                                c.getParameterTypes(),
                                                agentsServiceConfigurationParameterList));

        final Agent agent;
        context.setConfiguration(configuration);

        if (hasStrategyConstructor) {
            Constructor<?> agentConstructor =
                    agentClass.getConstructor(AgentComponentProvider.class);

            agent =
                    (Agent)
                            agentConstructor.newInstance(
                                    new AgentComponentProvider(
                                            tinkHttpClientProviderFactory
                                                    .createTinkHttpClientProvider(
                                                            request,
                                                            context,
                                                            configuration.getSignatureKeyPair()),
                                            supplementalInformationProviderFactory
                                                    .createSupplementalInformationProvider(
                                                            context, request),
                                            agentContextProviderFactory.createAgentContextProvider(
                                                    request, context)));

        } else if (hasAgentsServiceConfigurationConstructor) {
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
