package se.tink.backend.aggregation.agents.nxgen.it.banks.ing;

import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.framework.AgentTestServerSupplementalRequester;
import se.tink.backend.aggregation.agents.framework.context.NewAgentTestContext;
import se.tink.backend.aggregation.agents.framework.testserverclient.AgentTestServerClient;
import se.tink.backend.aggregation.configuration.IntegrationsConfiguration;
import se.tink.backend.aggregation.configuration.ProviderConfig;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.configuration.AgentConfigurationController;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.user.rpc.UserProfile;

class AgentTestFixtures {

    private static final String CREDENTIAL_ID = "cafebabecafebabecafebabecafebabe";
    private static final String USER_ID = "deadbeefdeadbeefdeadbeefdeadbeef";
    private static final String ORIGINATING_USER_IP = "127.0.0.1";
    private static final int TRANSACTIONS_TO_PRINT = 32;
    private static final String DEFAULT_LOCALE = "sv_SE";

    private static User givenUser() {
        UserProfile profile = new UserProfile();
        profile.setLocale(DEFAULT_LOCALE);

        User user = new User();
        user.setId(USER_ID);
        user.setProfile(profile);
        user.setFlags(Lists.newArrayList());

        return user;
    }

    static CredentialsRequest givenCredentialsRequest(
            String market, String providerName, Map<String, String> credentialFields) {
        Provider provider = givenProvider(market, providerName);

        return RefreshInformationRequest.builder()
                .user(givenUser())
                .provider(provider)
                .credentials(givenCredentials(credentialFields, provider))
                .originatingUserIp(ORIGINATING_USER_IP)
                .manual(true)
                .forceAuthenticate(false)
                .build();
    }

    static AgentContext givenAgentContext(
            CredentialsRequest givenCredentialsRequest,
            String redirectUrl,
            String clusterId,
            Provider provider) {
        NewAgentTestContext newAgentTestContext =
                new NewAgentTestContext(
                        givenCredentialsRequest.getUser(),
                        givenCredentialsRequest.getCredentials(),
                        new AgentTestServerSupplementalRequester(
                                givenCredentialsRequest.getCredentials(),
                                AgentTestServerClient.getInstance()),
                        TRANSACTIONS_TO_PRINT,
                        null,
                        clusterId,
                        provider);
        newAgentTestContext.setAgentConfigurationController(
                givenAgentConfigurationController(
                        givenCredentialsRequest.getProvider(), redirectUrl, clusterId));
        return newAgentTestContext;
    }

    private static Credentials givenCredentials(
            Map<String, String> credentialFields, Provider provider) {
        Credentials credentials = new Credentials();
        credentials.setFields(credentialFields);
        credentials.setId(CREDENTIAL_ID);
        credentials.setUserId(USER_ID);
        credentials.setStatus(CredentialsStatus.CREATED);
        credentials.setType(provider.getCredentialsType());
        credentials.setProviderName(provider.getName());
        return credentials;
    }

    static SignatureKeyPair givenSignatureKeyPair() {
        return new SignatureKeyPair();
    }

    private static Provider givenProvider(String market, String providerName) {
        ProviderConfig marketProviders = readProvidersConfiguration(market);
        Provider provider = marketProviders.getProvider(providerName);
        provider.setMarket(marketProviders.getMarket());
        provider.setCurrency(marketProviders.getCurrency());
        return provider;
    }

    private static String escapeMarket(String market) {
        return market.replaceAll("[^a-zA-Z]", "");
    }

    private static ProviderConfig readProvidersConfiguration(String market) {
        String providersFilePath =
                "external/tink_backend/src/provider_configuration/data/seeding/providers-"
                        + escapeMarket(market).toLowerCase()
                        + ".json";
        File providersFile = new File(providersFilePath);
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(providersFile, ProviderConfig.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static AgentConfigurationController givenAgentConfigurationController(
            Provider givenProvider, String redirectUrl, String clusterId) {
        return new AgentConfigurationController(
                mock(TppSecretsServiceClient.class),
                mock(IntegrationsConfiguration.class),
                givenProvider,
                null,
                clusterId,
                redirectUrl);
    }
}
