package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator;

import com.google.api.client.http.HttpStatusCodes;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc.BasicLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordnetPasswordAuthenticator implements PasswordAuthenticator {

    private final NordnetApiClient apiClient;
    private final SessionStorage sessionStorage;

    public NordnetPasswordAuthenticator(NordnetApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {

        LoginResponse loginResponse = init();

        BasicLoginRequest loginRequest = new BasicLoginRequest(username, password);

        RequestBuilder requestBuilder =
                apiClient
                        .createBasicRequest(new URL(Urls.BASIC_LOGIN))
                        .type(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.USER_AGENT, HeaderValues.NORDNET_AGENT)
                        .addBasicAuth(loginResponse.toBasicAuthHeader())
                        .body(loginRequest);
        try {
            HttpResponse response = apiClient.post(requestBuilder, HttpResponse.class);
            sessionStorage.put(HeaderKeys.NTAG, getNtag(response));
            sessionStorage.put(StorageKeys.SESSION_KEY, getSessionKey(response));

        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
            // re-throw unknown login error
            throw e;
        }
    }

    private String getNtag(HttpResponse response) throws LoginException {
        return response.getHeaders().get(HeaderKeys.NTAG).stream()
                .findFirst()
                .orElseThrow(LoginError.NOT_SUPPORTED::exception);
    }

    private String getSessionKey(HttpResponse response) throws LoginException {
        return Optional.of(
                        String.join(
                                ":",
                                response.getBody(LoginResponse.class).getSessionKey(),
                                response.getBody(LoginResponse.class).getSessionKey()))
                .orElseThrow(LoginError.NOT_SUPPORTED::exception);
    }

    private LoginResponse init() {

        RequestBuilder requestBuilder =
                apiClient
                        .createBasicRequest(new URL(Urls.INIT_LOGIN))
                        .accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(FormValues.ANONYMOUS_LOGIN.serialize());

        return apiClient.post(requestBuilder, HttpResponse.class).getBody(LoginResponse.class);
    }
}
