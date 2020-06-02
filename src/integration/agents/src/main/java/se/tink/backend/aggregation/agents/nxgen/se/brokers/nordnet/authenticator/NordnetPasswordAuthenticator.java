package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator;

import com.google.api.client.http.HttpStatusCodes;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc.AnonymousLoginPasswordResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc.AuthenticateBasicLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc.AuthenticateBasicLoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
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

        AnonymousLoginPasswordResponse anonymousLoginPasswordResponse = init();

        AuthenticateBasicLoginRequest loginRequest =
                new AuthenticateBasicLoginRequest(username, password);

        RequestBuilder requestBuilder =
                apiClient
                        .createBasicRequest(NordnetConstants.Urls.AUTHENTICATION_BASIC_LOGIN_URL)
                        .type(MediaType.APPLICATION_JSON)
                        .addBasicAuth(anonymousLoginPasswordResponse.toBasicAuthHeader())
                        .body(loginRequest);
        try {
            HttpResponse response = apiClient.post(requestBuilder, HttpResponse.class);
            sessionStorage.put(NordnetConstants.StorageKeys.NTAG, getNtag(response));
            sessionStorage.put(NordnetConstants.StorageKeys.SESSION_KEY, getSessionKey(response));

        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED
                    && e.getResponse()
                            .getBody(AuthenticateBasicLoginResponse.class)
                            .getCode()
                            .equalsIgnoreCase(NordnetConstants.Errors.INVALID_LOGIN_PARAMETER)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
            // re-throw unknown login error
            throw e;
        }
    }

    private String getNtag(HttpResponse response) throws LoginException {
        return response.getHeaders().get(NordnetConstants.HeaderKeys.NTAG).stream()
                .findFirst()
                .orElseThrow(LoginError.NOT_SUPPORTED::exception);
    }

    private String getSessionKey(HttpResponse response) throws LoginException {
        return Optional.of(
                        String.join(
                                ":",
                                response.getBody(AuthenticateBasicLoginResponse.class)
                                        .getSessionKey(),
                                response.getBody(AuthenticateBasicLoginResponse.class)
                                        .getSessionKey()))
                .orElseThrow(LoginError.NOT_SUPPORTED::exception);
    }

    private AnonymousLoginPasswordResponse init() {
        Form formData =
                Form.builder()
                        .put(
                                NordnetConstants.FormKeys.USERNAME,
                                NordnetConstants.FormValues.ANONYMOUS)
                        .put(
                                NordnetConstants.FormKeys.PASSWORD,
                                NordnetConstants.FormValues.ANONYMOUS)
                        .put(
                                NordnetConstants.FormKeys.SERVICE,
                                NordnetConstants.QueryParamValues.CLIENT_ID)
                        .put(
                                NordnetConstants.FormKeys.COUNTRY,
                                NordnetConstants.FormValues.COUNTRY_SE)
                        .put(
                                NordnetConstants.FormKeys.SESSION_LANGUAGE,
                                NordnetConstants.FormValues.LANG_EN)
                        .build();

        RequestBuilder requestBuilder =
                apiClient
                        .createBasicRequest(NordnetConstants.Urls.INIT_LOGIN_SESSION_URL_PASSWORD)
                        .accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(formData.serialize());

        return apiClient
                .post(requestBuilder, HttpResponse.class)
                .getBody(AnonymousLoginPasswordResponse.class);
    }
}
