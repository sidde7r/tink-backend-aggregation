package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.configuration.ChebancaConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class AuthenticatorTestBase {
    static final String CLIENT_ID = "client123";
    static final String CLIENT_SECRET = "clientSecret";
    static final String REDIRECT_URL = "http://foo.bar";
    static final String CERTIFICATE_ID = "certificateId";
    static final String APP_ID = "appId";
    static final String CLIENT_STATE = "clientState";
    static final int ERROR_RESPONSE_CODE = 500;
    ChebancaAuthenticator authenticator;

    void setUpAuthenticatorToCreateToken(HttpResponse response) {
        ChebancaApiClient apiClient = mock(ChebancaApiClient.class);
        when(apiClient.createToken(any())).thenReturn(response);
        ChebancaConfiguration config =
                new ChebancaConfiguration(
                        CLIENT_ID, CLIENT_SECRET, REDIRECT_URL, CERTIFICATE_ID, APP_ID);
        StrongAuthenticationState state = new StrongAuthenticationState(CLIENT_STATE);
        authenticator = new ChebancaAuthenticator(apiClient, config, state);
    }

    HttpResponse getMockedSuccessfulResponse(TokenResponse tokenResponse) {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getBody(TokenResponse.class)).thenReturn(tokenResponse);
        return response;
    }

    HttpResponse getMockedFailedResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(ERROR_RESPONSE_CODE);
        return response;
    }
}
