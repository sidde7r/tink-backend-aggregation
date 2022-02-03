package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.mockProxyResponseWithHeaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoStorage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.OauthTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.AuthenticationClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticationResult;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.integration.webdriver.service.proxy.ProxyResponse;

public class NordeaNoAuthenticatorTest {

    private static final String TEST_DATA_DIR =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/nordea/resources";
    private static final File OAUTH_TOKEN_RESPONSE_FILE =
            Paths.get(TEST_DATA_DIR, "oauthTokenResponse.json").toFile();

    /*
    Mocks
     */
    private AuthenticationClient authenticationClient;
    private NordeaNoStorage storage;

    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private NordeaNoAuthenticator authenticator;

    @Before
    public void setup() {
        authenticationClient = mock(AuthenticationClient.class);
        storage = mock(NordeaNoStorage.class);

        mocksToVerifyInOrder = inOrder(authenticationClient, storage);

        authenticator = new NordeaNoAuthenticator(authenticationClient, storage);
    }

    @Ignore
    public void should_extract_authorization_code_and_finish_manual_authentication() {
        // given
        OauthTokenResponse tokenResponse =
                deserializeFromFile(OAUTH_TOKEN_RESPONSE_FILE, OauthTokenResponse.class);
        when(authenticationClient.getOathToken(any(), any())).thenReturn(tokenResponse);

        when(storage.retrieveCodeVerifier()).thenReturn("STORAGE_CODE_VERIFIER");

        // when
        ProxyResponse proxyResponse =
                mockProxyResponseWithHeaders(
                        ImmutableMap.of(
                                "someKey1",
                                "someValue1",
                                "Location",
                                "http://redirect.url?key1=value1&code=AUTH_CODE&key2=value2",
                                "someKey2",
                                "someValue2"));
        BankIdIframeAuthenticationResult iframeAuthenticationResult =
                mock(BankIdIframeAuthenticationResult.class);
        when(iframeAuthenticationResult.getProxyResponseFromAuthFinishUrl())
                .thenReturn(proxyResponse);

        authenticator.handleBankIdAuthenticationResult(iframeAuthenticationResult);

        // then
        mocksToVerifyInOrder.verify(storage).retrieveCodeVerifier();
        mocksToVerifyInOrder
                .verify(authenticationClient)
                .getOathToken("AUTH_CODE", "STORAGE_CODE_VERIFIER");
        verifyStoresCorrectOauth2Token();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    private void verifyStoresCorrectOauth2Token() {
        ArgumentCaptor<OAuth2Token> tokenArgumentCaptor =
                ArgumentCaptor.forClass(OAuth2Token.class);
        mocksToVerifyInOrder.verify(storage).storeOauthToken(tokenArgumentCaptor.capture());
        OAuth2Token tokenStored = tokenArgumentCaptor.getValue();

        assertThat(tokenStored)
                .usingRecursiveComparison()
                .ignoringFields("issuedAt")
                .isEqualTo(
                        OAuth2Token.createBearer(
                                "SAMPLE_ACCESS_TOKEN", "SAMPLE_REFRESH_TOKEN", 180));
    }

    @Test
    public void should_throw_illegal_state_exception_on_missing_redirect_url_in_proxy_response() {
        // given
        OauthTokenResponse tokenResponse =
                deserializeFromFile(OAUTH_TOKEN_RESPONSE_FILE, OauthTokenResponse.class);
        when(authenticationClient.getOathToken(any(), any())).thenReturn(tokenResponse);

        // when
        ProxyResponse proxyResponse =
                mockProxyResponseWithHeaders(
                        ImmutableMap.of(
                                "someKey1", "someValue1",
                                "someKey2", "someValue2"));
        BankIdIframeAuthenticationResult iframeAuthenticationResult =
                mock(BankIdIframeAuthenticationResult.class);
        when(iframeAuthenticationResult.getProxyResponseFromAuthFinishUrl())
                .thenReturn(proxyResponse);

        Throwable throwable =
                catchThrowable(
                        () ->
                                authenticator.handleBankIdAuthenticationResult(
                                        iframeAuthenticationResult));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot find Location header in proxy response");

        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void
            should_throw_illegal_state_exception_on_missing_authorization_code_in_redirect_url() {
        // given
        OauthTokenResponse tokenResponse =
                deserializeFromFile(OAUTH_TOKEN_RESPONSE_FILE, OauthTokenResponse.class);
        when(authenticationClient.getOathToken(any(), any())).thenReturn(tokenResponse);

        // when
        ProxyResponse proxyResponse =
                mockProxyResponseWithHeaders(
                        ImmutableMap.of(
                                "someKey1", "someValue1",
                                "Location", "http://redirect.url?key1=value1&key2=value2",
                                "someKey2", "someValue2"));
        BankIdIframeAuthenticationResult iframeAuthenticationResult =
                mock(BankIdIframeAuthenticationResult.class);
        when(iframeAuthenticationResult.getProxyResponseFromAuthFinishUrl())
                .thenReturn(proxyResponse);

        Throwable throwable =
                catchThrowable(
                        () ->
                                authenticator.handleBankIdAuthenticationResult(
                                        iframeAuthenticationResult));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot extract authorization code from proxy response");

        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SneakyThrows
    @SuppressWarnings("SameParameterValue")
    private static <T> T deserializeFromFile(File file, Class<T> tClass) {
        return new ObjectMapper().readValue(file, tClass);
    }
}
