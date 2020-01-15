package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.configuration.ChebancaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception.UnsuccessfulApiCallException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class ChebancaAuthenticatorTest {
    private static final String CLIENT_ID = "client123";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String REDIRECT_URL = "http://foo.bar";
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String APP_ID = "appId";
    private static final String CLIENT_STATE = "clientState";
    private static final int ERROR_RESPONSE_CODE = 404;

    private ChebancaAuthenticator authenticator;

    @Test
    public void shouldReturnProperURLIfSuccessfulResponse() throws URISyntaxException {
        // given
        init(getMockedSuccessfulResponse());

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
        init(getMockedFailedResponse());

        // when
        Throwable thrown = catchThrowable(() -> authenticator.buildAuthorizeUrl(CLIENT_STATE));

        Assertions.assertThat(thrown)
                .isInstanceOf(UnsuccessfulApiCallException.class)
                .hasMessage(
                        "Could not perform redirect URL call to get the Login URL. Error response code: "
                                + ERROR_RESPONSE_CODE);
    }

    private void init(HttpResponse response) {
        ChebancaApiClient client = mock(ChebancaApiClient.class);
        when(client.getLoginUrl(getEndpointURL())).thenReturn(response);
        ChebancaConfiguration config =
                new ChebancaConfiguration(
                        CLIENT_ID, CLIENT_SECRET, REDIRECT_URL, CERTIFICATE_ID, APP_ID);
        StrongAuthenticationState state = new StrongAuthenticationState(CLIENT_STATE);

        this.authenticator = new ChebancaAuthenticator(client, config, state);
    }

    private HttpResponse getMockedSuccessfulResponse() throws URISyntaxException {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getLocation()).thenReturn(new URI(getLocationRedirectOfLoginEndpoint()));
        when(response.getStatus()).thenReturn(302);
        return response;
    }

    private HttpResponse getMockedFailedResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(ERROR_RESPONSE_CODE);
        return response;
    }

    private URL getEndpointURL() {
        return new URL(
                "https://external-api.chebanca.io/auth/oauth/v2/authorize?response_type=code&client_id"
                        + "=client123&redirect_uri=http%3A%2F%2Ffoo.bar&state=clientState");
    }

    private String getLocationRedirectOfLoginEndpoint() {
        return "https://clienti.chebanca.it/auth/oauth/v2/authorize/login?action=display&sessionID=1234-567"
                + "&sessionData=someSessionData&resourceId=12ab34cd";
    }
}
