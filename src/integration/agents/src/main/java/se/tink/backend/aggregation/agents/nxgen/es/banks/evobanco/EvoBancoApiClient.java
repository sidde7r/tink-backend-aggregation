package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco;

import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.KeepAliveResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LogoutResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

import javax.ws.rs.core.MediaType;

public class EvoBancoApiClient {

    private final TinkHttpClient client;

    public EvoBancoApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public LoginResponse login(LoginRequest loginRequest) throws LoginException {

        try {
            return createRequest(EvoBancoConstants.Urls.LOGIN)
                    .body(loginRequest, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                    .post(LoginResponse.class);
        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatus();

            if (statusCode == EvoBancoConstants.StatusCodes.INCORRECT_USERNAME_PASSWORD) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            throw e;
        }
    }

    private RequestBuilder createRequest(URL url) {

        return client
                .request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    public boolean isAlive(SessionStorage sessionStorage) throws SessionException {

        try {
            createRequest(EvoBancoConstants.Urls.KEEP_ALIVE.parameter(EvoBancoConstants.UrlParams.UID,
                        sessionStorage.get(EvoBancoConstants.Storage.USER_ID, String.class)
                                .orElseThrow(SessionError.SESSION_EXPIRED::exception)))
                    .addBearerToken(sessionStorage.get(EvoBancoConstants.Storage.ACCESS_TOKEN, OAuth2Token.class)
                            .orElseThrow(SessionError.SESSION_EXPIRED::exception))
                    .get(KeepAliveResponse.class);
        } catch (HttpResponseException e) {
            return false;
        }

        return true;
    }
}
