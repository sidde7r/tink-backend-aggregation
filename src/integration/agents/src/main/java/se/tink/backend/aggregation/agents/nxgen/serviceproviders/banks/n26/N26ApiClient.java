package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26;

import com.google.common.base.Strings;
import io.vavr.control.Either;
import io.vavr.control.Try;
import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26Constants.URLS;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.MultiFactorSelectRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.PasswordAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.app.AppPollRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.sms.MultiFactorWithOtpRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.MeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.SavingsAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.SavingsSpaceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.SpaceTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class N26ApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    Logger logger = LoggerFactory.getLogger(N26ApiClient.class);

    public N26ApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    private TokenEntity getToken() {
        TokenEntity token =
                sessionStorage
                        .get(N26Constants.Storage.TOKEN_ENTITY, TokenEntity.class)
                        .orElseThrow(() -> new NoSuchElementException("Token missing"));

        validateToken(token);
        return token;
    }

    private void validateToken(TokenEntity token) {
        if (!token.isValid()) {
            throw new IllegalStateException("Token is not valid!");
        }
    }

    private RequestBuilder getRequest(String resource, MediaType mediaType) {
        return getRequest(resource, N26Constants.BASIC_AUTHENTICATION_TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, mediaType);
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

    public String loginWithPassword(String username, String password) throws LoginException {
        String deviceId =
                persistentStorage
                        .get(N26Constants.Storage.DEVICE_TOKEN, String.class)
                        .orElseGet(
                                () -> {
                                    String id = UUID.randomUUID().toString();
                                    persistentStorage.put(N26Constants.Storage.DEVICE_TOKEN, id);
                                    return id;
                                });

        PasswordAuthenticationRequest request =
                new PasswordAuthenticationRequest(username, password);
        RequestBuilder requestBuilder =
                Try.of(() -> request.getBodyValue())
                        .recoverWith(
                                UnsupportedEncodingException.class,
                                N26Utils::handleUnsupportedEncodingException)
                        .map(b -> getRequestBody(deviceId, b))
                        .get();
        String mfaToken =
                Try.of(
                                () ->
                                        Either.<ErrorResponse, AuthenticationResponse>right(
                                                requestBuilder.post(AuthenticationResponse.class)))
                        .recoverWith(HttpResponseException.class, N26Utils::handleAgentError)
                        .get()
                        .fold(l -> l.getMfaToken(), r -> r.getMfaToken());

        return mfaToken;
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
        HttpResponse response =
                getRequest(N26Constants.URLS.SAVINGS, MediaType.APPLICATION_JSON_TYPE, bearer)
                        .get(HttpResponse.class);

        // N26 decided to send response with Content-Type =
        // text/plain,application/json;charset=UTF-8 . It leads to parsing exception. That's why
        // this header is removed and added application/json before deserialization

        response.getHeaders().remove("Content-Type");
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON);
        return response.getBody(SavingsAccountResponse.class);
    }

    public MeResponse fetchIdentityData() {
        TokenEntity token = getToken();
        String bearer = N26Constants.BEARER_TOKEN + token.getAccessToken();

        return getRequest(URLS.ME, MediaType.APPLICATION_JSON_TYPE, bearer).get(MeResponse.class);
    }

    public HttpResponse logout() {
        TokenEntity token = getToken();
        String bearer = N26Constants.BEARER_TOKEN + token.getAccessToken();
        HttpResponse result =
                getRequest(N26Constants.URLS.LOGOUT, MediaType.APPLICATION_JSON_TYPE, bearer)
                        .get(HttpResponse.class);
        sessionStorage.clear();
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

    public <T> T initiate2fa(String secondFactor, Class<T> className, String mfaToken) {

        String deviceToken =
                N26Utils.getFromStorage(
                        persistentStorage, N26Constants.Storage.DEVICE_TOKEN, String.class);
        MultiFactorSelectRequest request = new MultiFactorSelectRequest(secondFactor, mfaToken);
        return getRequest(
                        URLS.APP_AUTHENTICATION,
                        MediaType.APPLICATION_JSON_TYPE,
                        N26Constants.BASIC_AUTHENTICATION_TOKEN)
                .header(N26Constants.DEVICE_TOKEN, deviceToken)
                .post(className, request);
    }

    public Either<ErrorResponse, AuthenticationResponse> pollAppStatus() {
        String mfaToken =
                N26Utils.getFromStorage(
                        sessionStorage, N26Constants.Storage.MFA_TOKEN, String.class);
        String deviceToken =
                N26Utils.getFromStorage(
                        persistentStorage, N26Constants.Storage.DEVICE_TOKEN, String.class);
        AppPollRequest request = new AppPollRequest(mfaToken);

        RequestBuilder requestBuilder =
                Try.of(() -> request.getBodyValue())
                        .recoverWith(
                                UnsupportedEncodingException.class,
                                e -> N26Utils.handleUnsupportedEncodingException(e))
                        .map(b -> getRequestBody(deviceToken, b))
                        .get();

        return Try.of(
                        () ->
                                Either.<ErrorResponse, AuthenticationResponse>right(
                                        requestBuilder.post(AuthenticationResponse.class)))
                .recoverWith(
                        HttpClientException.class,
                        Try.failure(ThirdPartyAppError.TIMED_OUT.exception()))
                .recoverWith(HttpResponseException.class, N26Utils::handleAgentError)
                .get();
    }

    public Either<ErrorResponse, AuthenticationResponse> loginWithOtp(String otp) {
        String mfaToken =
                N26Utils.getFromStorage(
                        sessionStorage, N26Constants.Storage.MFA_TOKEN, String.class);
        String deviceToken =
                N26Utils.getFromStorage(
                        persistentStorage, N26Constants.Storage.DEVICE_TOKEN, String.class);
        MultiFactorWithOtpRequest request =
                new MultiFactorWithOtpRequest(N26Constants.Body.MultiFactor.MFA_OTP, mfaToken, otp);

        RequestBuilder requestBuilder =
                Try.of(() -> request.getBodyValue())
                        .recoverWith(
                                UnsupportedEncodingException.class,
                                N26Utils::handleUnsupportedEncodingException)
                        .map(b -> getRequestBody(deviceToken, b))
                        .get();

        return Try.<Either<ErrorResponse, AuthenticationResponse>>of(
                        () -> Either.right(requestBuilder.post(AuthenticationResponse.class)))
                .recoverWith(HttpResponseException.class, N26Utils::handleAgentError)
                .get();
    }

    private RequestBuilder getRequestBody(String deviceToken, String b) {
        return getRequest(URLS.BASE_AUTHENTICATION, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header(N26Constants.DEVICE_TOKEN, deviceToken)
                .body(b);
    }
}
