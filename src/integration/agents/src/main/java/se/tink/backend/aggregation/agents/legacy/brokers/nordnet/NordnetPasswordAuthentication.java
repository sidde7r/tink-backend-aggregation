package se.tink.backend.aggregation.agents.brokers.nordnet;

import com.google.common.base.Preconditions;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.Objects;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import se.tink.backend.aggregation.agents.brokers.nordnet.NordnetConstants.AnonymousLoginFormKeys;
import se.tink.backend.aggregation.agents.brokers.nordnet.NordnetConstants.AnonymousLoginFormValues;
import se.tink.backend.aggregation.agents.brokers.nordnet.NordnetConstants.QueryParamValues;
import se.tink.backend.aggregation.agents.brokers.nordnet.NordnetConstants.Urls;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.AnonymousLoginPasswordResponse;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.AuthenticateBasicLoginRequest;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.AuthenticateBasicLoginResponse;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.libraries.i18n.LocalizableKey;

public class NordnetPasswordAuthentication {
    private final NordnetApiClient apiClient;

    public NordnetPasswordAuthentication(NordnetApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public Optional<String> loginWithPassword(String username, String password)
            throws LoginException {
        AnonymousLoginPasswordResponse response = anonymousLoginForPassword();
        authenticate(username, password, response);
        // String authCode = authorizeUser();
        return Optional.empty(); // fetchToken(authCode);
    }

    private AnonymousLoginPasswordResponse anonymousLoginForPassword() throws LoginException {
        // This request will set the NOW cookie needed for subsequent requests
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add(AnonymousLoginFormKeys.USERNAME, AnonymousLoginFormValues.ANONYMOUS);
        formData.add(AnonymousLoginFormKeys.PASSWORD, AnonymousLoginFormValues.ANONYMOUS);
        formData.add(AnonymousLoginFormKeys.SERVICE, QueryParamValues.CLIENT_ID);
        formData.add(AnonymousLoginFormKeys.COUNTRY, AnonymousLoginFormValues.COUNTRY_SE);
        formData.add(AnonymousLoginFormKeys.SESSION_LANGUAGE, AnonymousLoginFormValues.LANG_EN);
        ClientResponse response =
                apiClient
                        .createClientRequest(
                                Urls.INIT_LOGIN_SESSION_URL_PASSWORD,
                                MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .post(ClientResponse.class, formData);

        AnonymousLoginPasswordResponse loginResponse =
                response.getEntity(AnonymousLoginPasswordResponse.class);
        Preconditions.checkState(
                loginResponse.getExpiresIn() > 0, "Expecting expiry to be larger than 0");
        if (response.getStatus() != HttpStatus.OK_200) {
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                    new LocalizableKey("Could not initiate login session."));
        }
        return loginResponse;
    }

    private void authenticate(
            String username,
            String password,
            AnonymousLoginPasswordResponse anonymousLoginPasswordResponse)
            throws LoginException {
        AuthenticateBasicLoginRequest loginRequest =
                new AuthenticateBasicLoginRequest(username, password);

        ClientResponse response =
                apiClient
                        .createClientRequest(
                                Urls.AUTHENTICATION_BASIC_LOGIN_URL,
                                MediaType.APPLICATION_JSON_TYPE)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                anonymousLoginPasswordResponse.toBasicAuthHeader())
                        .post(ClientResponse.class, loginRequest);

        AuthenticateBasicLoginResponse loginResponse =
                response.getEntity(AuthenticateBasicLoginResponse.class);

        if (Objects.equals(
                loginResponse.getCode(),
                NordnetConstants.Errors.NEXT_LOGIN_INVALID_LOGIN_PARAMETER)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        apiClient.manage(response);

        Preconditions.checkState(loginResponse.isLoggedIn(), "Expected user to be logged in");
        Preconditions.checkState(
                Objects.equals(
                        loginResponse.getSessionType(),
                        NordnetConstants.Session.TYPE_AUTHENTICATED),
                "Expected session to be of type authenticated");

        String ntag = response.getHeaders().getFirst(NordnetConstants.HeaderKeys.NTAG);
        Preconditions.checkNotNull(ntag, "Expected ntag header to exist for subsequent requests");
        Preconditions.checkNotNull(
                loginResponse.getSessionKey(),
                "Expected session key to exist for subsequent requests.");
        apiClient.setNtag(ntag);
        apiClient.setSessionKey(loginResponse.getSessionKey());
    }
}
