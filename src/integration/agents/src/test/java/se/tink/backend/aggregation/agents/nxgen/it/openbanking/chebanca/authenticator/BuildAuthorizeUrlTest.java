package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.data.ChebancaAuthenticatorTestData.getTokenResponseWithoutDataEntity;

import java.net.URI;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.configuration.ChebancaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception.UnsuccessfulApiCallException;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BuildAuthorizeUrlTest extends AuthenticatorTestBase {

    @Test
    public void shouldReturnProperURLIfSuccessfulResponse() {
        // given
        setUpAuthenticatorToBuildAuthUrl(
                getMockedSuccessfulResponse(getTokenResponseWithoutDataEntity()));

        // when
        URL loginURl = authenticator.buildAuthorizeUrl(CLIENT_STATE);

        // then
        assertEquals(
                new URL(
                        "https://clienti.chebanca.it/auth/oauth/v2/authorize/login?action=display&sessionID=1234-567&sessionData=someSessionData&resourceId=12ab34cd"),
                loginURl);
    }

    @Test
    public void shouldThrowIfUnSuccessfulResponse() {
        // given
        setUpAuthenticatorToBuildAuthUrl(getMockedFailedResponse());

        // when
        Throwable thrown = catchThrowable(() -> authenticator.buildAuthorizeUrl(CLIENT_STATE));

        Assertions.assertThat(thrown)
                .isInstanceOf(UnsuccessfulApiCallException.class)
                .hasMessage(
                        "Could not perform redirect URL call to get the Login URL. Error response code: "
                                + ERROR_RESPONSE_CODE);
    }

    @SneakyThrows
    @Override
    HttpResponse getMockedSuccessfulResponse(TokenResponse tokenResponse) {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getLocation()).thenReturn(new URI(getLocationRedirectOfLoginEndpoint()));
        when(response.getStatus()).thenReturn(302);
        return response;
    }

    private void setUpAuthenticatorToBuildAuthUrl(HttpResponse response) {
        ChebancaApiClient apiClient = mock(ChebancaApiClient.class);
        AgentConfiguration<ChebancaConfiguration> agentConfiguration =
                mock(AgentConfiguration.class);
        when(apiClient.getLoginUrl(any())).thenReturn(response);
        ChebancaConfiguration config =
                new ChebancaConfiguration(
                        CLIENT_ID, CLIENT_SECRET, APP_ID, CLIENT_ID, CLIENT_SECRET, APP_ID);
        when(agentConfiguration.getProviderSpecificConfiguration()).thenReturn(config);
        when(agentConfiguration.getRedirectUrl()).thenReturn(REDIRECT_URL);
        StrongAuthenticationState state = new StrongAuthenticationState(CLIENT_STATE);
        authenticator = new ChebancaOAuth2Authenticator(apiClient, agentConfiguration, state);
    }

    private String getLocationRedirectOfLoginEndpoint() {
        return "https://clienti.chebanca.it/auth/oauth/v2/authorize/login?action=display&sessionID=1234-567"
                + "&sessionData=someSessionData&resourceId=12ab34cd";
    }
}
