package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26;

import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.AuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.SavingsAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class N26ApiClient {

    private final TinkHttpClient client;
    private final SessionStorage storage;
    Logger logger = LoggerFactory.getLogger(N26ApiClient.class);

    public N26ApiClient(TinkHttpClient client, SessionStorage storage) {
        this.client = client;
        this.storage = storage;
    }

    public boolean tokenExists() {
        return storage.containsKey(N26Constants.Storage.TOKEN_ENTITY);
    }

    private TokenEntity getToken() {
        TokenEntity token = storage.get(N26Constants.Storage.TOKEN_ENTITY, TokenEntity.class)
                .orElseThrow(() -> new NoSuchElementException("Token missing"));

        validateToken(token);
        return token;
    }

    private void validateToken(TokenEntity token) {
        if (!token.isValid()) {
            logger.error("Token is not valid! {}", token);
            throw new IllegalStateException("Token is not valid!");
        }
    }

    private RequestBuilder getRequest(String resource, String token) {
        return client.request(getUrl(resource))
                .header(HttpHeaders.AUTHORIZATION, token);
    }

    private RequestBuilder getRequest(String resource, MediaType mediaType, String token) {
        return getRequest(resource, token)
                .header(HttpHeaders.CONTENT_TYPE, mediaType);
    }

    private URL getUrl(String resource) {
        return new URL(N26Constants.URLS.HOST + resource);
    }

    public void login(String username, String password) throws LoginException {
        AuthenticationRequest request = new AuthenticationRequest(username, password);
        AuthenticationResponse response;
        try {
            response = getRequest(N26Constants.URLS.AUTHENTICATION, MediaType.APPLICATION_FORM_URLENCODED_TYPE,
                    N26Constants.BASIC_AUTHENTICATION_TOKEN)
                    .post(AuthenticationResponse.class, request.getBody());

        } catch (UnsupportedEncodingException e) {
            logger.error("Unable to encode {}", e);
            throw new IllegalStateException("Unable to encode ", e);
        } catch (HttpResponseException e) {
            AuthenticationResponse errResponse = e.getResponse().getBody(AuthenticationResponse.class);
            if (N26Constants.AUTHENTICATION_ERROR.equalsIgnoreCase(errResponse.getError())) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
            logger.error("Unable to authenticate error {} description {} {}", errResponse.getError(),
                    errResponse.getErrorDescription(), e);
            throw e;
        }
        storage.put(N26Constants.Storage.TOKEN_ENTITY, response.getToken());
    }

    public AccountResponse fetchAccounts() {
        TokenEntity token = getToken();
        String bearer = N26Constants.BEARER_TOKEN + token.getAccessToken();

        return getRequest(N26Constants.URLS.ACCOUNT, MediaType.APPLICATION_JSON_TYPE, bearer)
                .get(AccountResponse.class);
    }

    public HttpResponse checkIfSessionAlive() {
        TokenEntity token = getToken();
        String bearer = N26Constants.BEARER_TOKEN + token.getAccessToken();

        return getRequest(N26Constants.URLS.ACCOUNT, MediaType.APPLICATION_JSON_TYPE, bearer)
                .get(HttpResponse.class);
    }

    public TransactionResponse fetchTransactions() {
        TokenEntity token = getToken();
        String bearer = N26Constants.BEARER_TOKEN + token.getAccessToken();

        return getRequest(N26Constants.URLS.TRANSACTION, MediaType.APPLICATION_JSON_TYPE, bearer)
                .get(TransactionResponse.class);
    }

    public SavingsAccountResponse fetchSavingsAccounts() {
        TokenEntity token = getToken();
        String bearer = N26Constants.BEARER_TOKEN + token.getAccessToken();

        return getRequest(N26Constants.URLS.SAVINGS, MediaType.APPLICATION_JSON_TYPE, bearer)
                .get(SavingsAccountResponse.class);
    }

    public void fetchAndLogCreditInfo() {
        TokenEntity tokenEntity = getToken();
        String bearer = N26Constants.BEARER_TOKEN + tokenEntity.getAccessToken();
        try {
            String creditEg = getRequest(N26Constants.URLS.CREDIT_ELIGIBILITY, bearer)
                    .get(String.class);
            logger.info("{} response: {}", N26Constants.Logging.CREDIT_ELIGIBILITY, creditEg);
            String creditDraft = getRequest(N26Constants.URLS.CREDIT_DRAFTS, bearer)
                    .queryParam(N26Constants.Queryparams.FLOW_VERSION, N26Constants.Queryparams.FLOW_VERSION_V2)
                    .get(String.class);
            logger.info("{} response: {}", N26Constants.Logging.CREDIT_DRAFT, creditDraft);
            String fullInfo = getRequest(N26Constants.URLS.USER_FULL_INFO, bearer)
                    .queryParam(N26Constants.Queryparams.FULL, N26Constants.Queryparams.FULL_TRUE)
                    .get(String.class);
            logger.info("{} response: {}", N26Constants.Logging.FULL_USER_INFO, fullInfo);

        } catch (Exception e) {
            logger.warn("{} error: {}", N26Constants.Logging.CREDIT_ERROR, e.toString());
        }
    }

    public HttpResponse logout() {
        TokenEntity token = getToken();
        String bearer = N26Constants.BEARER_TOKEN + token.getAccessToken();
        HttpResponse result = getRequest(N26Constants.URLS.LOGOUT, MediaType.APPLICATION_JSON_TYPE, bearer)
                .get(HttpResponse.class);
        storage.clear();
        return result;
    }
}
