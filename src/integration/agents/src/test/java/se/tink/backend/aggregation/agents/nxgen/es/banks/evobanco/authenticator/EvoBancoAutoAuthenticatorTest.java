package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities.ErrorEntity;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EvoBancoAutoAuthenticatorTest {
    private static final String ERROR_RESPONSE_FOR_403_STATUS =
            "{\"incidentId\":\"dummy\",\"hostName\":\"dummy\",\"errorCode\":\"15\",\"description\":\"This request was blocked by the security rules\",\"timeUtc\":\"dummy\",\"clientIp\":\"dummy\",\"proxyId\":\"dummy\",\"proxyIp\":\"dummy\"}";
    private static final String ERROR_RESPONSE_FOR_OTHER_STATUSES =
            "{\"code\":\"400\",\"message\":\"Authentication Error\"}";
    private static final String ERROR_RESPONSE_UNKNOWN =
            "{\"code\":\"409\",\"message\":\"Something bad happened\"}";
    private EvoBancoAutoAuthenticator evoBancoAutoAuthenticator;
    private EvoBancoApiClient evoBancoApiClient;
    private Credentials credentials;

    @Before
    public void setup() {
        evoBancoApiClient = mock(EvoBancoApiClient.class);
        credentials = mock(Credentials.class);
        evoBancoAutoAuthenticator =
                new EvoBancoAutoAuthenticator(
                        evoBancoApiClient,
                        credentials,
                        mock(PersistentStorage.class),
                        mock(SessionStorage.class));
    }

    @Test(expected = AuthorizationException.class)
    public void shouldThrowAuthorizationExceptionFor403HttpStatus() {
        // given
        setUpCredentialsMock();
        setUpHttpClientMockForAuthenticationExceptionByStatusAndResponse(
                403, ERROR_RESPONSE_FOR_403_STATUS);
        // when
        evoBancoAutoAuthenticator.autoAuthenticate();
    }

    @Test(expected = AuthorizationException.class)
    public void shouldThrowAuthorizationExceptionForAuthorizationErrorMessage() {
        // given
        setUpCredentialsMock();
        setUpHttpClientMockForAuthenticationExceptionByStatusAndResponse(
                400, ERROR_RESPONSE_FOR_OTHER_STATUSES);
        // when
        evoBancoAutoAuthenticator.autoAuthenticate();
    }

    @Test(expected = SessionException.class)
    public void shouldThrowSessionExceptionForOtherErrors() {
        // given
        setUpCredentialsMock();
        setUpHttpClientMockForAuthenticationExceptionByStatusAndResponse(
                409, ERROR_RESPONSE_UNKNOWN);
        // when
        evoBancoAutoAuthenticator.autoAuthenticate();
    }

    private void setUpCredentialsMock() {
        when(credentials.getField(Field.Key.USERNAME)).thenReturn("DUMMY");
        when(credentials.getField(Field.Key.PASSWORD)).thenReturn("DUMMY");
    }

    private void setUpHttpClientMockForAuthenticationExceptionByStatusAndResponse(
            int httpStatus, String responseBody) {
        final HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(httpStatus);
        when(httpResponse.getBody(ErrorEntity.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(responseBody, ErrorEntity.class));
        when(evoBancoApiClient.login(any()))
                .thenThrow(new HttpResponseException(null, httpResponse));
    }
}
