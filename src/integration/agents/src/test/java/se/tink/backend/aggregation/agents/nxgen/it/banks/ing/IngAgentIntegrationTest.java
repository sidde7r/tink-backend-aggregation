package se.tink.backend.aggregation.agents.nxgen.it.banks.ing;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static se.tink.backend.agents.rpc.Field.Key.PASSWORD;
import static se.tink.backend.agents.rpc.Field.Key.USERNAME;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.AgentTestFixtures.givenAgentContext;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.AgentTestFixtures.givenCredentialsRequest;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.AgentTestFixtures.givenSignatureKeyPair;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.IngAgentConstants.DATE_OF_BIRTH;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IngAgentIntegrationTest extends AgentIntegrationTest {

    public IngAgentIntegrationTest() {
        super(
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/banks/ing/resources");
    }

    @Test
    public void testAgentLoginFirstTimeSoRegistrationProcess() throws Exception {
        // given
        IngModuleDependenciesRegistration testBeansRegistration =
                prepareTestModuleDependenciesWithMocks();
        CredentialsRequest givenCredentialsRequest =
                givenCredentialsRequest(
                        "it",
                        "it-ing-other",
                        ImmutableMap.of(
                                USERNAME.getFieldKey(),
                                "123456",
                                PASSWORD.getFieldKey(),
                                "987654",
                                DATE_OF_BIRTH,
                                "01012000"));

        recordLogin1ServerResponse();
        recordLogin2ServerResponse();

        // when
        IngAgent agent =
                new IngAgent(
                        testBeansRegistration,
                        givenCredentialsRequest,
                        givenAgentContext(
                                givenCredentialsRequest,
                                null,
                                "oxford-staging",
                                givenCredentialsRequest.getProvider()),
                        givenSignatureKeyPair());
        SteppableAuthenticationResponse response =
                agent.login(
                        SteppableAuthenticationRequest.initialRequest(
                                givenCredentialsRequest.getCredentials()));
        agent.persistLoginSession();

        // then
        assertThat(response.getStepIdentifier()).isEqualTo(Optional.empty());
        assertThat(response.getSupplementInformationRequester()).isEqualTo(null);
        assertSessionStorageState(givenCredentialsRequest);
    }

    private void assertSessionStorageState(CredentialsRequest givenCredentialsRequest) {
        Optional<String> sessionStorage =
                givenCredentialsRequest.getCredentials().getSensitivePayload(Key.SESSION_STORAGE);
        assertThat(sessionStorage.isPresent()).isTrue();
        assertThat(sessionStorage.get())
                .isEqualTo(
                        "{\"jSessionId\":\"PgAAAAhDN1Aa1gEibmukwfG1h\",\"DeviceId\":\"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=\"}");
    }

    private void recordLogin1ServerResponse() {
        wireMockRule.stubFor(
                any(urlPathEqualTo("/MobileFlow/login1.htm"))
                        .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                        .withHeader("X-OTML-PROFILE", equalTo("appstore"))
                        .withHeader("X-OTML-NONCE", equalTo("1"))
                        .withHeader(
                                "User-Agent",
                                equalTo(
                                        "Mozilla/5.0 (iPhone; U; CPU OS 3_2 like Mac OS X; en-us) "
                                                + "AppleWebKit/531.21.10 (KHTML, like Gecko) "
                                                + "Version/4.0.4 Mobile/7B334b Safari/531.21.10"))
                        .withHeader("X-OTMLID", equalTo("1.07"))
                        .withHeader("X-OTML-ADVANCED-MANIFEST", equalTo("true"))
                        .withHeader("X-APPID", equalTo("iPhone_Ing_41_3.0.15"))
                        .withHeader("X-OTML-PLATFORM", equalTo("ios"))
                        .withHeader("X-OTML-CLUSTER", equalTo("{750, 1334}"))
                        .withHeader("Accept-Language", equalTo("it-IT, it-IT;q=0.5"))
                        .withHeader("Accept", equalTo("*/*"))
                        .withHeader("Accept-Encoding", equalTo("br, gzip, deflate"))
                        .withRequestBody(
                                equalTo(getTestResourceFileContent("login1_call_request_body.txt")))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "text/otml;charset=UTF-8")
                                        .withHeader("Date", "Tue, 03 Dec 2019 10:16:57 GMT")
                                        .withHeader("Server", "Apache-Coyote/1.1")
                                        .withHeader("X-Frame-Options", "DENY")
                                        .withHeader("X-FRAME-OPTIONS", "DENY")
                                        .withHeader(
                                                "X-Content-Security-Policy",
                                                "allow *; options inline-script eval-script; frame-ancestors 'self';")
                                        .withHeader("X-Content-Type-Options", "nosniff")
                                        .withHeader("Vary", "Accept-Encoding")
                                        .withHeader("Pragma", "no-cache")
                                        .withHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT")
                                        .withHeader("Cache-Control", "no-cache")
                                        .withHeader("IngAction", "login1")
                                        .withHeader("X-OTMLSign", "otmlsign=")
                                        .withHeader("Content-Language", "it-IT")
                                        .withHeader(
                                                "Set-Cookie",
                                                "ADRUM_BTa=\"val1\"; Version=1; Max-Age=30; Path=/")
                                        .withHeader(
                                                "Set-Cookie",
                                                "ADRUM_BTa=\"val2\"; Version=1; Max-Age=30; Path=/")
                                        .withHeader(
                                                "Set-Cookie",
                                                "ADRUM_BT1=\"val3\"; Version=1; Max-Age=30; Path=/")
                                        .withHeader(
                                                "Set-Cookie",
                                                "ADRUM_BT1=\"val4\"; Version=1; Max-Age=30; Path=/")
                                        .withHeader("Keep-Alive", "timeout=15, max=100")
                                        .withHeader("Connection", "keep-alive")
                                        .withStatus(200)
                                        .withBody(
                                                getTestResourceFileContent(
                                                        "login1_call_response_body.json"))));
    }

    private void recordLogin2ServerResponse() {
        wireMockRule.stubFor(
                any(urlPathEqualTo("/MobileFlow/login2.htm"))
                        .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                        .withHeader("X-OTML-PROFILE", equalTo("appstore"))
                        .withHeader("X-OTML-NONCE", equalTo("1"))
                        .withHeader(
                                "User-Agent",
                                equalTo(
                                        "Mozilla/5.0 (iPhone; U; CPU OS 3_2 like Mac OS X; en-us) "
                                                + "AppleWebKit/531.21.10 (KHTML, like Gecko) "
                                                + "Version/4.0.4 Mobile/7B334b Safari/531.21.10"))
                        .withHeader("X-OTMLID", equalTo("1.07"))
                        .withHeader("X-OTML-ADVANCED-MANIFEST", equalTo("true"))
                        .withHeader("X-APPID", equalTo("iPhone_Ing_41_3.0.15"))
                        .withHeader("X-OTML-PLATFORM", equalTo("ios"))
                        .withHeader("X-OTML-CLUSTER", equalTo("{750, 1334}"))
                        .withHeader("Accept-Language", equalTo("it-IT, it-IT;q=0.5"))
                        .withHeader("Accept", equalTo("*/*"))
                        .withHeader("Accept-Encoding", equalTo("br, gzip, deflate"))
                        .withRequestBody(
                                equalTo(getTestResourceFileContent("login2_call_request_body.txt")))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "text/html; charset=UTF-8")
                                        .withHeader("Date", "Tue, 03 Dec 2019 10:17:54 GMT")
                                        .withHeader("Server", "Apache-Coyote/1.1")
                                        .withHeader("X-Frame-Options", "DENY")
                                        .withHeader("X-FRAME-OPTIONS", "DENY")
                                        .withHeader(
                                                "X-Content-Security-Policy",
                                                "allow *; options inline-script eval-script; frame-ancestors 'self';")
                                        .withHeader("X-Content-Type-Options", "nosniff")
                                        .withHeader("Vary", "Accept-Encoding")
                                        .withHeader("IngAction", "login2")
                                        .withHeader("Content-Language", "it-IT")
                                        .withHeader(
                                                "Set-Cookie",
                                                "ADRUM_BTa=\"val1\"; Version=1; Max-Age=30; Path=/")
                                        .withHeader(
                                                "Set-Cookie",
                                                "ADRUM_BTa=\"val2\"; Version=1; Max-Age=30; Path=/")
                                        .withHeader(
                                                "Set-Cookie",
                                                "ADRUM_BT1=\"val3\"; Version=1; Max-Age=30; Path=/")
                                        .withHeader(
                                                "Set-Cookie",
                                                "ADRUM_BT1=\"val4\"; Version=1; Max-Age=30; Path=/")
                                        .withHeader(
                                                "Set-Cookie",
                                                "JSESSIONID=PgAAAAhDN1Aa1gEibmukwfG1h; Path=/MobileFlow;Secure")
                                        .withHeader("Keep-Alive", "timeout=15, max=100")
                                        .withHeader("Connection", "keep-alive")
                                        .withStatus(200)));
    }

    private IngModuleDependenciesRegistration prepareTestModuleDependenciesWithMocks() {
        RandomDataProvider randomDataProvider = spy(RandomDataProvider.class);
        when(randomDataProvider.generateRandomBytes(32)).thenReturn(new byte[32]);
        when(randomDataProvider.generateRandomBytes(16)).thenReturn(new byte[16]);

        ConfigurationProvider configurationProvider = spy(ConfigurationProvider.class);
        when(configurationProvider.getBaseUrl())
                .thenReturn(String.format("http://localhost:%s", wireMockRule.port()));
        when(configurationProvider.useRsaWithPadding()).thenReturn(false);

        return new IngModuleDependenciesRegistration() {
            protected ConfigurationProvider configurationProvider() {
                return configurationProvider;
            }

            protected RandomDataProvider randomDataProvider() {
                return randomDataProvider;
            }
        };
    }
}
