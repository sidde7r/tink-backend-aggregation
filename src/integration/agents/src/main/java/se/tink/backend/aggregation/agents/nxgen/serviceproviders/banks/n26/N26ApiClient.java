package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26;

import com.google.common.base.Strings;
import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26Constants.URLS;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.AuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.MeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.SavingsAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.SavingsSpaceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.SpaceTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.identitydata.IdentityData;

public class N26ApiClient implements IdentityDataFetcher {

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
        TokenEntity token =
                storage.get(N26Constants.Storage.TOKEN_ENTITY, TokenEntity.class)
                        .orElseThrow(() -> new NoSuchElementException("Token missing"));

        validateToken(token);
        return token;
    }

    private void validateToken(TokenEntity token) {
        if (!token.isValid()) {
            throw new IllegalStateException("Token is not valid!");
        }
    }

    private RequestBuilder getRequest(String resource, String token) {
        return client.request(getUrl(resource)).header(HttpHeaders.AUTHORIZATION, token);
    }

    private RequestBuilder getRequest(String resource, MediaType mediaType, String token) {
        return getRequest(resource, token).header(HttpHeaders.CONTENT_TYPE, mediaType);
    }

    private URL getUrl(String resource) {
        return new URL(N26Constants.URLS.HOST + resource);
    }

    public void login(String username, String password) throws LoginException {
        AuthenticationRequest request = new AuthenticationRequest(username, password);
        AuthenticationResponse response;
        try {
            response =
                    getRequest(
                                    N26Constants.URLS.AUTHENTICATION,
                                    MediaType.APPLICATION_FORM_URLENCODED_TYPE,
                                    N26Constants.BASIC_AUTHENTICATION_TOKEN)
                            .post(AuthenticationResponse.class, request.getBody());

        } catch (UnsupportedEncodingException e) {
            logger.error("Unable to encode {}", e);
            throw new IllegalStateException("Unable to encode ", e);
        } catch (HttpResponseException e) {
            AuthenticationResponse errResponse =
                    e.getResponse().getBody(AuthenticationResponse.class);
            if (N26Constants.AUTHENTICATION_ERROR.equalsIgnoreCase(errResponse.getError())) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
            logger.error(
                    "Unable to authenticate error {} description {} {}",
                    errResponse.getError(),
                    errResponse.getErrorDescription(),
                    e);
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

    public TransactionResponse fetchTransactions(String lastTransactionId) {
        TokenEntity token = getToken();
        String bearer = N26Constants.BEARER_TOKEN + token.getAccessToken();

        if (!Strings.isNullOrEmpty(lastTransactionId)) {
            TransactionResponse response =
                    getRequest(
                                    N26Constants.URLS.TRANSACTION,
                                    MediaType.APPLICATION_JSON_TYPE,
                                    bearer)
                            .queryParam(
                                    N26Constants.Queryparams.LIMIT,
                                    N26Constants.Queryparams.TRANSACTION_LIMIT_DEFAULT)
                            .queryParam(N26Constants.Queryparams.LASTID, lastTransactionId)
                            .get(TransactionResponse.class);
            response.setPreviousTransactionId(lastTransactionId);
            return response;
        }

        return getRequest(N26Constants.URLS.TRANSACTION, MediaType.APPLICATION_JSON_TYPE, bearer)
                .queryParam(
                        N26Constants.Queryparams.LIMIT,
                        N26Constants.Queryparams.TRANSACTION_LIMIT_DEFAULT)
                .get(TransactionResponse.class);
    }

    public SavingsAccountResponse fetchSavingsAccounts() {
        TokenEntity token = getToken();
        String bearer = N26Constants.BEARER_TOKEN + token.getAccessToken();

        return getRequest(N26Constants.URLS.SAVINGS, MediaType.APPLICATION_JSON_TYPE, bearer)
                .get(SavingsAccountResponse.class);
    }

    @Override
    public IdentityData fetchIdentityData() {
        TokenEntity token = getToken();
        String bearer = N26Constants.BEARER_TOKEN + token.getAccessToken();

        return getRequest(URLS.ME, MediaType.APPLICATION_JSON_TYPE, bearer)
                .get(MeResponse.class)
                .toTinkIdentity();
    }

    public HttpResponse logout() {
        TokenEntity token = getToken();
        String bearer = N26Constants.BEARER_TOKEN + token.getAccessToken();
        HttpResponse result =
                getRequest(N26Constants.URLS.LOGOUT, MediaType.APPLICATION_JSON_TYPE, bearer)
                        .get(HttpResponse.class);
        storage.clear();
        return result;
    }

    public SavingsSpaceResponse fetchSavingsSpaceAccounts() {
        TokenEntity token = getToken();
        String bearer = N26Constants.BEARER_TOKEN + token.getAccessToken();

        return getRequest(N26Constants.URLS.SPACES_SAVINGS, MediaType.APPLICATION_JSON_TYPE, bearer)
                .get(SavingsSpaceResponse.class);
    }

    public SpaceTransactionResponse fetchSpaceTransactions(
            String spaceId, String lastTransactionId) {
        TokenEntity token = getToken();
        String bearer = N26Constants.BEARER_TOKEN + token.getAccessToken();
        URL url = new URL(N26Constants.URLS.SPACES_TRANSACTIONS).parameter("spaceId", spaceId);
        if (Strings.isNullOrEmpty(lastTransactionId)) {
            return getRequest(url.toString(), MediaType.APPLICATION_JSON_TYPE, bearer)
                    .queryParam(
                            N26Constants.Queryparams.SPACE_TRANSACTIONS_SIZE,
                            N26Constants.Queryparams.SPACE_LIMIT_DEFAULT)
                    .get(SpaceTransactionResponse.class);
        } else {
            return getRequest(url.toString(), MediaType.APPLICATION_JSON_TYPE, bearer)
                    .queryParam(
                            N26Constants.Queryparams.SPACE_TRANSACTIONS_SIZE,
                            N26Constants.Queryparams.SPACE_LIMIT_DEFAULT)
                    .queryParam(N26Constants.Queryparams.SPACE_BEFOREID, lastTransactionId)
                    .get(SpaceTransactionResponse.class);
        }
    }
}
