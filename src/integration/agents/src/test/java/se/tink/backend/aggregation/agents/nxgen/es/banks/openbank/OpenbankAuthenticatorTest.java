package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.OpenbankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class OpenbankAuthenticatorTest {
    @Test
    public void testAuthenticationSuccess() throws AuthenticationException, AuthorizationException {
        final OpenbankApiClient apiClient = mock(OpenbankApiClient.class);
        final LoginResponse loginResponse =
                SerializationUtils.deserializeFromString(
                        "{\"tokenCredential\":\"aHVudGVyMg==\"}", LoginResponse.class);
        when(apiClient.login(any())).thenReturn(loginResponse);
        final SessionStorage sessionStorage = new SessionStorage();
        final OpenbankAuthenticator authenticator =
                new OpenbankAuthenticator(apiClient, sessionStorage);
        authenticator.authenticate(mockCredentials());

        assertEquals("aHVudGVyMg==", sessionStorage.get(Storage.AUTH_TOKEN));
    }

    @Test(expected = LoginException.class)
    public void testAuthenticationFailure() throws Exception {
        final OpenbankApiClient apiClient = mock(OpenbankApiClient.class);
        final HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getBody(ErrorResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"error\":\"bad.input.credentials.incorrect\",\"error_description\":null}",
                                ErrorResponse.class));
        when(apiClient.login(any())).thenThrow(new HttpResponseException(null, httpResponse));

        final OpenbankAuthenticator authenticator =
                new OpenbankAuthenticator(apiClient, new SessionStorage());
        authenticator.authenticate(mockCredentials());
    }

    private Credentials mockCredentials() {
        final Credentials credentials = mock(Credentials.class);
        when(credentials.getField(Field.Key.USERNAME)).thenReturn("username");
        when(credentials.getField(Field.Key.PASSWORD)).thenReturn("password");
        when(credentials.getField(OpenbankConstants.USERNAME_TYPE)).thenReturn("N");
        return credentials;
    }
}
