package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.client;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.APP_VERSION;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.DA_LANGUAGE;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.HeaderValues;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.Headers;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.Uri;

import agents_platform_agents_framework.org.springframework.http.HttpMethod;
import agents_platform_agents_framework.org.springframework.http.MediaType;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.RequestEntity.BodyBuilder;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.LunarTestUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.entites.NemIdEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.AccessTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.AccessTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.NemIdParamsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.SignInRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class AuthenticationApiClientTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/lunar/resources";

    private static final String CHALLENGE = "1234567890123";
    private static final String DEVICE_ID = "some test id";
    private static final String TOKEN = "test token";
    private static final String LUNAR_PASSWORD = "1111";
    private static final String UNFORMATTED_SIGNATURE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<ds:SignatureValue>\nabcdefghij+abcdef/O002/</ds:SignatureValue>";

    private AgentPlatformHttpClient client;
    private RandomValueGenerator randomValueGenerator;
    private AuthenticationApiClient authenticationApiClient;
    private BodyBuilder requestBuilder;

    @Before
    public void setup() {
        client = mock(AgentPlatformHttpClient.class);
        randomValueGenerator = new MockRandomValueGenerator();
        authenticationApiClient = new AuthenticationApiClient(client, randomValueGenerator);
    }

    @Test
    @Parameters(method = "getNemIdParametersTestParams")
    public void shouldGetNemIdParameters(String nemIdParamsString, NemIdParamsResponse expected) {
        // given
        requestBuilder = createTestBodyBuilder(HttpMethod.GET, Uri.NEM_ID_AUTHENTICATE);

        // and
        when(client.exchange(requestBuilder.build(), String.class))
                .thenReturn(ResponseEntity.ok(nemIdParamsString));

        // when
        NemIdParamsResponse result = authenticationApiClient.getNemIdParameters(DEVICE_ID);

        // then
        assertThat(result).isEqualTo(expected);
    }

    private Object[] getNemIdParametersTestParams() throws IOException {
        return new Object[] {
            new Object[] {
                FileUtils.readFileToString(
                        Paths.get(TEST_DATA_PATH, "nem_id_parameters.json").toFile(),
                        StandardCharsets.UTF_8),
                LunarTestUtils.getExpectedNemIdParamsResponse()
            },
            new Object[] {null, new NemIdParamsResponse()},
        };
    }

    private BodyBuilder createTestBodyBuilder(HttpMethod httpMethod, URI uri) {
        return RequestEntity.method(httpMethod, uri)
                .header(Headers.DEVICE_MODEL, HeaderValues.DEVICE_MODEL)
                .header(Headers.USER_AGENT, HeaderValues.USER_AGENT_VALUE)
                .header(Headers.REGION, HeaderValues.DK_REGION)
                .header(Headers.OS, HeaderValues.I_OS)
                .header(Headers.DEVICE_MANUFACTURER, HeaderValues.DEVICE_MANUFACTURER)
                .header(Headers.OS_VERSION, HeaderValues.OS_VERSION)
                .header(Headers.LANGUAGE, DA_LANGUAGE)
                .header(Headers.REQUEST_ID, randomValueGenerator.getUUID().toString())
                .header(Headers.DEVICE_ID, DEVICE_ID)
                .header(Headers.ACCEPT_LANGUAGE, HeaderValues.DA_LANGUAGE_ACCEPT)
                .header(Headers.ORIGIN, HeaderValues.APP_ORIGIN)
                .header(Headers.APP_VERSION, APP_VERSION)
                .header(Headers.ACCEPT_ENCODING, HeaderValues.ENCODING)
                .accept(MediaType.ALL);
    }

    @Test
    @Parameters(method = "postNemIdTokenParams")
    public void shouldPostNemIdToken(
            String accessTokenResponseString, AccessTokenResponse expected) {
        // given
        requestBuilder =
                createTestBodyBuilder(HttpMethod.POST, Uri.NEM_ID_AUTHENTICATE)
                        .contentType(MediaType.APPLICATION_JSON);

        // and
        AccessTokenRequest accessTokenRequest =
                new AccessTokenRequest(new NemIdEntity(UNFORMATTED_SIGNATURE, CHALLENGE));

        // and
        when(client.exchange(
                        requestBuilder.body(getAccessTokenRequestFormatted(accessTokenRequest)),
                        String.class))
                .thenReturn(ResponseEntity.ok(accessTokenResponseString));

        // when
        AccessTokenResponse result =
                authenticationApiClient.postNemIdToken(UNFORMATTED_SIGNATURE, CHALLENGE, DEVICE_ID);

        // then
        assertThat(result).isEqualTo(expected);
    }

    private String getAccessTokenRequestFormatted(AccessTokenRequest accessTokenRequest) {
        return StringUtils.replace(
                SerializationUtils.serializeToString(accessTokenRequest), "/", "\\/");
    }

    private AccessTokenResponse getExpectedAccessTokenResponse() {
        AccessTokenResponse expected = new AccessTokenResponse();
        expected.setAccessToken("this_is_test_access_token");
        expected.setGuid("4b52ed08-7207-4709-97b1-cf09f8eba5c6");
        expected.setIntercomHmac("123456abc7890fedcba");
        return expected;
    }

    private Object[] postNemIdTokenParams() throws IOException {
        return new Object[] {
            new Object[] {
                FileUtils.readFileToString(
                        Paths.get(TEST_DATA_PATH, "access_token_response.json").toFile(),
                        StandardCharsets.UTF_8),
                getExpectedAccessTokenResponse()
            },
            new Object[] {null, new AccessTokenResponse()},
        };
    }

    @Test
    @Parameters(method = "signInToLunarParams")
    public void shouldSignInToLunar(String tokenResponseString, TokenResponse expected) {
        // given
        requestBuilder =
                createTestBodyBuilder(HttpMethod.POST, Uri.SIGN_IN)
                        .header(Headers.AUTHORIZATION, TOKEN)
                        .header(Headers.CONTENT_TYPE, APPLICATION_JSON);

        // and
        when(client.exchange(requestBuilder.body(new SignInRequest(LUNAR_PASSWORD)), String.class))
                .thenReturn(ResponseEntity.ok(tokenResponseString));

        // when
        TokenResponse result = authenticationApiClient.signIn(LUNAR_PASSWORD, TOKEN, DEVICE_ID);

        // then
        assertThat(result).isEqualTo(expected);
    }

    private TokenResponse getExpectedTokenResponse() {
        TokenResponse expected = new TokenResponse();
        expected.setToken("test token");
        return expected;
    }

    private Object[] signInToLunarParams() throws IOException {
        return new Object[] {
            new Object[] {
                FileUtils.readFileToString(
                        Paths.get(TEST_DATA_PATH, "token_response.json").toFile(),
                        StandardCharsets.UTF_8),
                getExpectedTokenResponse()
            },
            new Object[] {null, new TokenResponse()},
        };
    }
}
