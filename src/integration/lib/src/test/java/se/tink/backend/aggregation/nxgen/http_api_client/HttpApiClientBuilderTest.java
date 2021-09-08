package se.tink.backend.aggregation.nxgen.http_api_client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static se.tink.backend.aggregation.nxgen.controllers.configuration.AgentConfigurationController.REDIRECT_URL_KEY;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.aggregation_agent_api_client.src.http.HttpApiClient;
import se.tink.libraries.aggregation_agent_api_client.src.variable.VariableKey;

public class HttpApiClientBuilderTest {

    private HttpApiClientBuilder builder;
    private static final String TEST_API_KEY = "dummyApiKey";
    private static final String TEST_CLIENT_ID = "dummyClientId";
    private static final String TEST_CLIENT_SECRET = "dummyClientSecret";
    private static final String TEST_REDIRECT_URL = "https://example.com/callback";
    private static Map<String, Object> TEST_SECRETS =
            ImmutableMap.<String, Object>builder()
                    .put("clientId", TEST_CLIENT_ID)
                    .put("clientSecret", TEST_CLIENT_SECRET)
                    .put("apiKey", TEST_API_KEY)
                    .put(REDIRECT_URL_KEY, TEST_REDIRECT_URL)
                    .build();
    private static Map<String, Object> EMPTY_SECRETS = ImmutableMap.of();

    @Before
    public void setUp() throws Exception {
        EidasProxyConfiguration eidasProxyConfiguration =
                EidasProxyConfiguration.createLocal("https://127.0.0.1");
        EidasIdentity eidasIdentity =
                new EidasIdentity(
                        "oxford-staging",
                        "5f98e87106384b2981c0354a33b51590",
                        "DEFAULT",
                        "xx-tink-testing",
                        HttpApiClientBuilderTest.class);
        this.builder =
                new HttpApiClientBuilder()
                        .setEidasIdentity(eidasIdentity)
                        .setEidasProxyConfiguration(eidasProxyConfiguration)
                        .setLogMasker(new FakeLogMasker())
                        .setLogOutputStream(System.out)
                        .setUseEidasProxy(false);
    }

    @Test
    public void testNullUserIp() {
        HttpApiClient apiClient =
                builder.setUserIp(null)
                        .setSecretsConfiguration(EMPTY_SECRETS)
                        .setPersistentStorage(mock(PersistentStorage.class))
                        .build();

        assertFalse(apiClient.getVariable(VariableKey.PSU_IP_ADDRESS).isPresent());
    }

    @Test
    public void testUserIp() {
        String userIp = "12.34.56.78";
        HttpApiClient apiClient =
                builder.setUserIp(userIp)
                        .setSecretsConfiguration(EMPTY_SECRETS)
                        .setPersistentStorage(mock(PersistentStorage.class))
                        .build();

        assertEquals(userIp, apiClient.getVariable(VariableKey.PSU_IP_ADDRESS).get());
    }

    @Test
    public void testBuildWithSecrets() {
        HttpApiClient apiClient =
                builder.setUserIp(null)
                        .setSecretsConfiguration(TEST_SECRETS)
                        .setPersistentStorage(mock(PersistentStorage.class))
                        .build();

        assertEquals(TEST_CLIENT_ID, apiClient.getVariable(VariableKey.CLIENT_ID).get());
        assertEquals(TEST_CLIENT_SECRET, apiClient.getVariable(VariableKey.CLIENT_SECRET).get());
        assertEquals(TEST_API_KEY, apiClient.getVariable(VariableKey.API_KEY).get());
        assertEquals(TEST_REDIRECT_URL, apiClient.getVariable(VariableKey.REDIRECT_URI).get());
    }

    @Test
    public void testBuildWithPersistentStorage() {
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put("consentId", "dummyConsentId");
        HttpApiClient apiClient =
                builder.setUserIp(null)
                        .setSecretsConfiguration(EMPTY_SECRETS)
                        .setPersistentStorage(persistentStorage)
                        .build();

        assertEquals("dummyConsentId", apiClient.getVariable(VariableKey.CONSENT_ID).get());
    }
}
