package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.configuration.SkandiaConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SkandiaApiClientTest {

    TinkHttpClient tinkHttpClient;
    PersistentStorage persistentStorage;
    SkandiaUserIpInformation skandiaUserIpInformation;

    SkandiaApiClient skandiaApiClient;

    @Before
    public void setup() {
        tinkHttpClient = mock(TinkHttpClient.class);
        persistentStorage = mock(PersistentStorage.class);
        skandiaUserIpInformation = mock(SkandiaUserIpInformation.class);

        skandiaApiClient =
                new SkandiaApiClient(tinkHttpClient, persistentStorage, skandiaUserIpInformation);
    }

    @Test
    public void testAuthorizeUrlIsConstructedProperlyWhenProvidedOneScope() {
        EidasProxyConfiguration eidasProxyConfiguration = mock(EidasProxyConfiguration.class);
        this.skandiaApiClient.setConfiguration(
                new AgentConfiguration.Builder<SkandiaConfiguration>()
                        .setProviderSpecificConfiguration(
                                new SkandiaConfiguration(
                                        "clientId", "clientSecret", Collections.singleton("AIS")))
                        .setRedirectUrl("http://redirect.tink")
                        .build(),
                eidasProxyConfiguration);

        assertThat(skandiaApiClient.getAuthorizeUrl("state"))
                .asString()
                .isEqualTo(
                        "https://fsts.skandia.se/as/authorization.oauth2?client_id=clientId&redirect_uri=http%3A%2F%2Fredirect.tink&scope=psd2.aisp&state=state&response_type=code");
    }

    @Test
    public void testAuthorizeUrlContainsAISAndPISScopesWhenPISScopeIsAddedInConfig() {
        EidasProxyConfiguration eidasProxyConfiguration = mock(EidasProxyConfiguration.class);
        this.skandiaApiClient.setConfiguration(
                new AgentConfiguration.Builder<SkandiaConfiguration>()
                        .setProviderSpecificConfiguration(
                                new SkandiaConfiguration(
                                        "clientId", "clientSecret", Collections.singleton("PIS")))
                        .setRedirectUrl("http://redirect.tink")
                        .build(),
                eidasProxyConfiguration);

        assertThat(skandiaApiClient.getAuthorizeUrl("state"))
                .asString()
                .containsOnlyOnce("psd2.pisp")
                .containsOnlyOnce("psd2.aisp");
    }

    @Test
    public void testAuthorizeUrlContainsAISAndPISScopesWhenBothScopesAreInConfig() {
        EidasProxyConfiguration eidasProxyConfiguration = mock(EidasProxyConfiguration.class);
        this.skandiaApiClient.setConfiguration(
                new AgentConfiguration.Builder<SkandiaConfiguration>()
                        .setProviderSpecificConfiguration(
                                new SkandiaConfiguration(
                                        "clientId",
                                        "clientSecret",
                                        io.vavr.collection.HashSet.of("AIS", "PIS").toJavaSet()))
                        .setRedirectUrl("http://redirect.tink")
                        .build(),
                eidasProxyConfiguration);

        assertThat(skandiaApiClient.getAuthorizeUrl("state"))
                .asString()
                .containsOnlyOnce("psd2.pisp")
                .containsOnlyOnce("psd2.aisp");
    }

    @Test
    public void testAuthorizeUrlContainsDefaultAISScopeWhenNoneAreProvided() {
        EidasProxyConfiguration eidasProxyConfiguration = mock(EidasProxyConfiguration.class);
        this.skandiaApiClient.setConfiguration(
                new AgentConfiguration.Builder<SkandiaConfiguration>()
                        .setProviderSpecificConfiguration(
                                new SkandiaConfiguration("clientId", "clientSecret", null))
                        .setRedirectUrl("http://redirect.tink")
                        .build(),
                eidasProxyConfiguration);

        assertThat(skandiaApiClient.getAuthorizeUrl("state"))
                .asString()
                .containsOnlyOnce("psd2.aisp");
    }

    @Test
    public void testAuthorizeUrlThrowsErrorWhenScopeIsInvalid() {
        Set<String> invalidScopes = Collections.singleton("INVALID_SCOPE");
        EidasProxyConfiguration eidasProxyConfiguration = mock(EidasProxyConfiguration.class);
        this.skandiaApiClient.setConfiguration(
                new AgentConfiguration.Builder<SkandiaConfiguration>()
                        .setProviderSpecificConfiguration(
                                new SkandiaConfiguration("clientId", "clientSecret", invalidScopes))
                        .setRedirectUrl("http://redirect.tink")
                        .build(),
                eidasProxyConfiguration);

        assertThatThrownBy(() -> skandiaApiClient.getAuthorizeUrl("state"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("INVALID_SCOPE");
    }
}
