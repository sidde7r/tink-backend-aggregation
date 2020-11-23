package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta;

import com.google.common.base.Strings;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ArgentaErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ConfigResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.StartAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.StartAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ValidateAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ValidateAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.rpc.ArgentaAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.rpc.ArgentaTransactionResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.LocalizableKey;

public class ArgentaApiClient {

    private final TinkHttpClient client;
    private final ArgentaSessionStorage sessionStorage;
    private static final Logger LOGGER = LoggerFactory.getLogger(ArgentaApiClient.class);

    public ArgentaApiClient(TinkHttpClient client, ArgentaSessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public void keepAlive(String deviceId) throws SessionException {
        RequestBuilder request = client.request(ArgentaConstants.Url.KEEP_ALIVE_URL);
        addMandatoryHeaders(request, deviceId);
        setAuthorization(request);
        HttpResponse response = request.post(HttpResponse.class);
        if (response.getStatus() == 204) {
            storeAuthorization(response);
            return;
        }
        throw SessionError.SESSION_EXPIRED.exception();
    }

    public ConfigResponse getConfig(URL config, String deviceToken) {
        RequestBuilder request =
                client.request(config)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE);
        addMandatoryHeaders(request, deviceToken);
        return getRequestWithAuthorization(ConfigResponse.class, request);
    }

    public StartAuthResponse startAuth(
            URL authStart, StartAuthRequest registrationRequest, String deviceToken)
            throws LoginException, AuthorizationException {
        RequestBuilder request =
                client.request(authStart)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE);
        addMandatoryHeaders(request, deviceToken);
        return postRequestWithAuthorization(StartAuthResponse.class, request, registrationRequest);
    }

    public ValidateAuthResponse validateAuth(
            ValidateAuthRequest validateAuthRequest, String deviceToken)
            throws LoginException, AuthorizationException {
        RequestBuilder request =
                client.request(ArgentaConstants.Url.AUTH_VALIDATE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .type(MediaType.APPLICATION_JSON_TYPE);
        addMandatoryHeaders(request, deviceToken);
        return postRequestWithAuthorization(
                ValidateAuthResponse.class, request, validateAuthRequest);
    }

    public ArgentaAccountResponse fetchAccounts(int page, String deviceId) {
        RequestBuilder request =
                client.request(ArgentaConstants.Url.ACCOUNTS)
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        if (page > 1) request.queryParam(ArgentaConstants.PARAMETERS.PAGE, Integer.toString(page));

        addMandatoryHeaders(request, deviceId);
        return getRequestWithAuthorization(ArgentaAccountResponse.class, request);
    }

    public ArgentaTransactionResponse fetchTransactions(
            String accountId, int page, String deviceId) {
        URL transactions =
                ArgentaConstants.Url.ACCOUNTS
                        .concat("/")
                        .concat(accountId)
                        .concat(ArgentaConstants.Url.TRANSACTIONS);
        RequestBuilder request =
                client.request(transactions).accept(MediaType.APPLICATION_JSON_TYPE);
        request.queryParam(ArgentaConstants.PARAMETERS.PAGE, Integer.toString(page));
        addMandatoryHeaders(request, deviceId);
        return getRequestWithAuthorization(ArgentaTransactionResponse.class, request);
    }

    private void addMandatoryHeaders(RequestBuilder builder, String deviceId) {
        for (Map.Entry<String, String> header : ArgentaConstants.HEADERS.entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }
        builder.header(ArgentaConstants.HEADER.DEVICE_ID, deviceId);
    }

    private void addAuthorizationHeader(RequestBuilder builder, String authorization) {
        builder.header(ArgentaConstants.HEADER.AUTHORIZATION, "Bearer " + authorization);
    }

    private <T, R> T postRequestWithAuthorization(
            Class<T> responseClass, RequestBuilder request, R post)
            throws LoginException, AuthorizationException {
        setAuthorization(request);
        try {
            HttpResponse response = request.post(HttpResponse.class, post);
            storeAuthorization(response);
            return response.getBody(responseClass);
        } catch (HttpResponseException responseException) {
            HttpResponse response = responseException.getResponse();
            ArgentaErrorResponse argentaErrorResponse =
                    response.getBody(ArgentaErrorResponse.class);
            handleKnownErrorResponses(argentaErrorResponse, responseException);
            LOGGER.warn(getErrorMessage(argentaErrorResponse), responseException);
            throw LoginError.DEFAULT_MESSAGE.exception(
                    new LocalizableKey(getErrorMessage(argentaErrorResponse)), responseException);
        }
    }

    void handleKnownErrorResponses(
            ArgentaErrorResponse argentaErrorResponse, HttpResponseException responseException)
            throws LoginException, AuthorizationException {
        String errorCode = argentaErrorResponse.getCode();
        if (!Strings.isNullOrEmpty(errorCode)) {
            String value = errorCode.toLowerCase();
            if (value.startsWith(ArgentaConstants.ErrorResponse.AUTHENTICATION)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(responseException);
            } else if (value.startsWith(ArgentaConstants.ErrorResponse.ERROR_CODE_SBB)) {
                String errorMessage = getErrorMessage(argentaErrorResponse);
                if (!Strings.isNullOrEmpty(errorMessage)) {
                    handleKnownErrorMessages(errorMessage.toLowerCase(), responseException);
                }
            } else if (value.startsWith(ArgentaConstants.ErrorResponse.ERROR_INVALID_REQUEST)) {
                // happens when app version is too old
                throw new IllegalArgumentException(value, responseException);
            }
        }
    }

    private void handleKnownErrorMessages(
            String errorMessage, HttpResponseException responseException)
            throws LoginException, AuthorizationException {
        if (errorMessage.contains(ArgentaConstants.ErrorResponse.TOO_MANY_DEVICES)) {
            throw LoginError.REGISTER_DEVICE_ERROR.exception(responseException);
        } else if (errorMessage.contains(ArgentaConstants.ErrorResponse.AUTHENTICATION_ERROR)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(responseException);
        } else if (errorMessage.contains(ArgentaConstants.ErrorResponse.TOO_MANY_ATTEMPTS)) {
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(responseException);
        } else if (errorMessage.contains(ArgentaConstants.ErrorResponse.ACCOUNT_BLOCKED)) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(responseException);
        } else if (errorMessage.contains(
                ArgentaConstants.ErrorResponse.PROBLEM_SOLVING_IN_PROGRESS)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(responseException);
        } else if (errorMessage.contains(ArgentaConstants.ErrorResponse.SOMETHING_WRONG)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(responseException);
        }
    }

    private String getErrorMessage(ArgentaErrorResponse argentaErrorResponse) {
        if (argentaErrorResponse.getFieldErrors() != null
                && argentaErrorResponse.getFieldErrors().size() >= 1) {
            return argentaErrorResponse.getFieldErrors().get(0).getMessage();
        }
        return argentaErrorResponse.getMessage();
    }

    private void setAuthorization(RequestBuilder request) {
        if (!Strings.isNullOrEmpty(sessionStorage.getAuthorization()))
            addAuthorizationHeader(request, sessionStorage.getAuthorization());
    }

    private <T> T getRequestWithAuthorization(Class<T> responseClass, RequestBuilder request) {
        setAuthorization(request);
        HttpResponse response = request.get(HttpResponse.class);
        return response.getBody(responseClass);
    }

    private void storeAuthorization(HttpResponse response) {
        String authorization =
                response.getHeaders().getFirst(ArgentaConstants.HEADER.AUTHORIZATION);
        sessionStorage.setAuthorization(authorization);
    }
}
