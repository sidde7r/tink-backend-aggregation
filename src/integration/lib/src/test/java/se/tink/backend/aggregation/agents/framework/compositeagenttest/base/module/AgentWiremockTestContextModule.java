package se.tink.backend.aggregation.agents.framework.compositeagenttest.base.module;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import java.util.Map;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.ProviderSessionCacheContext;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.provider.SupplementalInformationControllerProvider;
import se.tink.backend.aggregation.agents.framework.context.NewAgentTestContext;
import se.tink.backend.aggregation.agents.framework.provider.ProviderConfigurationUtil;
import se.tink.backend.aggregation.configuration.ProviderConfig;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.configuration.AgentConfigurationController;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;
import se.tink.backend.aggregation.nxgen.controllers.utils.MockSessionCacheProvider;
import se.tink.backend.aggregation.nxgen.controllers.utils.MockSupplementalRequester;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.raw_data_events.event_producers.RawBankDataEventAccumulator;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceInternalClient;
import se.tink.backend.integration.tpp_secrets_service.client.TppSecretsServiceInternalClientImpl;
import se.tink.backend.integration.tpp_secrets_service.client.configuration.TppSecretsServiceConfiguration;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.user.rpc.UserProfile;

public final class AgentWiremockTestContextModule extends AbstractModule {
    private static final String DEFAULT_CREDENTIAL_ID = "cafebabecafebabecafebabecafebabe";
    private static final String DEFAULT_USER_ID = "deadbeefdeadbeefdeadbeefdeadbeef";
    private static final String DEFAULT_LOCALE = "sv_SE";
    private static final String DEFAULT_APP_ID = "tink";
    private static final String DEFAULT_ORIGINATING_USER_IP = "127.0.0.1";

    private final MarketCode marketCode;
    private final String providerName;
    private final AgentsServiceConfiguration configuration;
    private final Map<String, String> loginDetails;
    private final String credentialPayload;
    private final Map<String, String> callbackData;
    private final Map<String, String> persistentStorageData;
    private final Map<String, String> sessionStorageData;
    private final Map<String, String> cache;
    private final boolean httpDebugTraceEnabled;
    private final RawBankDataEventAccumulator rawBankDataEventAccumulator;

    public AgentWiremockTestContextModule(
            MarketCode marketCode,
            String providerName,
            AgentsServiceConfiguration configuration,
            Map<String, String> loginDetails,
            String credentialPayload,
            Map<String, String> callbackData,
            Map<String, String> persistentStorageData,
            Map<String, String> sessionStorageData,
            Map<String, String> cache,
            boolean httpDebugTraceEnabled,
            RawBankDataEventAccumulator rawBankDataEventAccumulator) {
        this.marketCode = marketCode;
        this.providerName = providerName;
        this.configuration = configuration;
        this.loginDetails = loginDetails;
        this.credentialPayload = credentialPayload;
        this.callbackData = callbackData;
        this.persistentStorageData = persistentStorageData;
        this.sessionStorageData = sessionStorageData;
        this.cache = cache;
        this.httpDebugTraceEnabled = httpDebugTraceEnabled;
        this.rawBankDataEventAccumulator = rawBankDataEventAccumulator;
    }

    @Override
    protected void configure() {
        bind(AgentContext.class).to(NewAgentTestContext.class).in(Scopes.SINGLETON);
        bind(Boolean.class)
                .annotatedWith(Names.named("httpDebugTraceEnabled"))
                .toInstance(httpDebugTraceEnabled);
        bind(AgentsServiceConfiguration.class).toInstance(configuration);
        bind(TppSecretsServiceConfiguration.class)
                .toInstance(configuration.getTppSecretsServiceConfiguration());
        bind(SupplementalInformationController.class)
                .toProvider(SupplementalInformationControllerProvider.class)
                .in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    @SupplementalInformationCallbackData
    private Map<String, String> supplementalInformationCallbackData() {
        return callbackData;
    }

    @Provides
    @Singleton
    private SupplementalRequester provideSupplementalRequester() {
        return new MockSupplementalRequester(callbackData);
    }

    @Provides
    @Singleton
    private ProviderSessionCacheContext providerSessionCacheContext() {
        return new MockSessionCacheProvider(cache);
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
        ProviderConfig marketProviders =
                ProviderConfigurationUtil.readProvidersConfiguration(
                        marketCode.toString().toLowerCase());
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
        credential.setPayload(credentialPayload);
        credential.setSensitivePayload(
                Field.Key.PERSISTENT_STORAGE,
                SerializationUtils.serializeToString(persistentStorageData));
        credential.setSensitivePayload(
                Field.Key.SESSION_STORAGE,
                SerializationUtils.serializeToString(sessionStorageData));
        return credential;
    }

    @Provides
    @Singleton
    private String provideOriginatingUserIp() {
        return DEFAULT_ORIGINATING_USER_IP;
    }

    @Provides
    @Singleton
    public AgentConfigurationControllerable provideAgentConfigurationControllerable(
            Provider provider) {

        final ManagedTppSecretsServiceInternalClient tppSecretsServiceInternalClient =
                new TppSecretsServiceInternalClientImpl(
                        configuration.getTppSecretsServiceConfiguration());
        try {
            tppSecretsServiceInternalClient.start();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return new AgentConfigurationController(
                tppSecretsServiceInternalClient,
                configuration.getIntegrations(),
                provider,
                DEFAULT_APP_ID,
                "oxford-preprod",
                "DEFAULT",
                null,
                false);
    }

    @Provides
    @Singleton
    private NewAgentTestContext providerNewAgentTestContext(
            User user,
            Credentials credential,
            Provider provider,
            AgentConfigurationControllerable agentConfigurationControllerable,
            SupplementalRequester supplementalRequester,
            ProviderSessionCacheContext providerSessionCacheContext) {
        NewAgentTestContext context =
                new NewAgentTestContext(
                        user,
                        credential,
                        supplementalRequester,
                        providerSessionCacheContext,
                        32,
                        DEFAULT_APP_ID,
                        "oxford-preprod",
                        provider);
        context.setAgentConfigurationController(agentConfigurationControllerable);
        context.setRawBankDataEventAccumulator(rawBankDataEventAccumulator);
        return context;
    }
}
