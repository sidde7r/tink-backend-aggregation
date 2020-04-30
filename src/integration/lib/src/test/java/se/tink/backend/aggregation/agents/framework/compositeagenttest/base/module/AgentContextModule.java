package se.tink.backend.aggregation.agents.framework.compositeagenttest.base.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.framework.NewAgentTestContext;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.provider.SupplementalInformationControllerProvider;
import se.tink.backend.aggregation.configuration.ProviderConfig;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.configuration.AgentConfigurationController;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;
import se.tink.backend.aggregation.nxgen.controllers.utils.MockSupplementalRequester;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.backend.integration.tpp_secrets_service.client.TppSecretsServiceClientImpl;
import se.tink.backend.integration.tpp_secrets_service.client.configuration.TppSecretsServiceConfiguration;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.user.rpc.UserProfile;

public final class AgentContextModule extends AbstractModule {
    private static final String DEFAULT_CREDENTIAL_ID = "cafebabecafebabecafebabecafebabe";
    private static final String DEFAULT_USER_ID = "deadbeefdeadbeefdeadbeefdeadbeef";
    private static final String DEFAULT_LOCALE = "sv_SE";
    private static final String DEFAULT_APP_ID = "tink";

    private final MarketCode marketCode;
    private final String providerName;
    private final AgentsServiceConfiguration configuration;
    private final Map<String, String> loginDetails;

    public AgentContextModule(
            MarketCode marketCode,
            String providerName,
            AgentsServiceConfiguration configuration,
            Map<String, String> loginDetails) {
        this.marketCode = marketCode;
        this.providerName = providerName;
        this.configuration = configuration;
        this.loginDetails = loginDetails;
    }

    @Override
    protected void configure() {
        bind(AgentContext.class).to(NewAgentTestContext.class).in(Scopes.SINGLETON);
        bind(AgentsServiceConfiguration.class).toInstance(configuration);
        bind(TppSecretsServiceConfiguration.class)
                .toInstance(configuration.getTppSecretsServiceConfiguration());
        bind(SupplementalInformationController.class)
                .toProvider(SupplementalInformationControllerProvider.class)
                .in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    private User provideUser() {
        UserProfile profile = new UserProfile();
        profile.setLocale(DEFAULT_LOCALE);
        User user = new User();
        user.setId(DEFAULT_USER_ID);
        user.setProfile(profile);
        user.setFlags(Lists.newArrayList());
        return user;
    }

    @Provides
    @Singleton
    private Provider provideProvider() {
        ProviderConfig marketProviders = readProvidersConfiguration(marketCode);
        final Provider provider = marketProviders.getProvider(providerName);
        provider.setMarket(marketProviders.getMarket());
        provider.setCurrency(marketProviders.getCurrency());
        return provider;
    }

    @Provides
    @Singleton
    private Credentials provideCredentials(Provider provider) {
        Credentials credential = new Credentials();
        credential.setId(DEFAULT_CREDENTIAL_ID);
        credential.setUserId(DEFAULT_USER_ID);
        credential.setStatus(CredentialsStatus.CREATED);
        credential.setProviderName(provider.getName());
        credential.setType(provider.getCredentialsType());
        credential.setFields(loginDetails);

        return credential;
    }

    @Provides
    @Singleton
    public AgentConfigurationControllerable provideAgentConfigurationControllerable(
            Provider provider) {

        final ManagedTppSecretsServiceClient tppSecretsServiceClient =
                new TppSecretsServiceClientImpl(configuration.getTppSecretsServiceConfiguration());
        try {
            tppSecretsServiceClient.start();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return new AgentConfigurationController(
                tppSecretsServiceClient,
                configuration.getIntegrations(),
                provider,
                DEFAULT_APP_ID,
                "oxford-preprod",
                null);
    }

    @Provides
    @Singleton
    private NewAgentTestContext providerNewAgentTestContext(
            User user,
            Credentials credential,
            Provider provider,
            AgentConfigurationControllerable agentConfigurationControllerable) {
        NewAgentTestContext context =
                new NewAgentTestContext(
                        user,
                        credential,
                        new MockSupplementalRequester(),
                        32,
                        DEFAULT_APP_ID,
                        "oxford-preprod",
                        provider);
        context.setAgentConfigurationController(agentConfigurationControllerable);
        return context;
    }

    private static ProviderConfig readProvidersConfiguration(
            se.tink.libraries.enums.MarketCode market) {
        String providersFilePath =
                "external/tink_backend/src/provider_configuration/data/seeding/providers-"
                        + market.toString().toLowerCase()
                        + ".json";
        File providersFile = new File(providersFilePath);
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(providersFile, ProviderConfig.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
