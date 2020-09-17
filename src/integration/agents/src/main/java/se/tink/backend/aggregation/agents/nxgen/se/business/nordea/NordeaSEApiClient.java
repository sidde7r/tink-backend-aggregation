package se.tink.backend.aggregation.agents.nxgen.se.business.nordea;

import java.util.Date;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.FormParams;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.HeaderParams;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.BankIdAutostartResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.FetchCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.FetchTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.FetchTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.InitBankIdAutostartRequest;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc.ResultBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.entities.Form;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.identitydata.rpc.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.rpc.FetchAccountTransactionResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class NordeaSEApiClient {
    private final TinkHttpClient httpClient;
    private final SessionStorage sessionStorage;

    public NordeaSEApiClient(TinkHttpClient httpClient, SessionStorage sessionStorage) {
        this.httpClient = httpClient;
        this.sessionStorage = sessionStorage;
    }

    public BankIdAutostartResponse initBankIdAutostart(
            InitBankIdAutostartRequest initBankIdAutostartRequest) {
        return httpClient
                .request(Urls.LOGIN_BANKID_AUTOSTART)
                .headers(NordeaSEConstants.NORDEA_CUSTOM_HEADERS)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(initBankIdAutostartRequest, MediaType.APPLICATION_JSON_TYPE)
                .post(BankIdAutostartResponse.class);
    }

    public BankIdAutostartResponse pollBankIdAutostart(String sessionId) {
        return httpClient
                .request(Urls.LOGIN_BANKID_AUTOSTART.concat(sessionId))
                .headers(NordeaSEConstants.NORDEA_CUSTOM_HEADERS)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(BankIdAutostartResponse.class);
    }

    public BankIdAutostartResponse fetchLoginCode(FetchCodeRequest fetchCodeRequest) {
        return httpClient
                .request(Urls.FETCH_LOGIN_CODE)
                .headers(NordeaSEConstants.NORDEA_CUSTOM_HEADERS)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(fetchCodeRequest, MediaType.APPLICATION_JSON_TYPE)
                .post(BankIdAutostartResponse.class);
    }

    public ResultBankIdResponse fetchAccessToken(String code, String codeVerifier) {
        Form form = new Form(NordeaSEConstants.REQUEST_TOKEN_FORM);
        form.put(FormParams.CODE, code);
        form.put(FormParams.CODE_VERIFIER, codeVerifier);

        return fetchAccessToken(form);
    }

    private ResultBankIdResponse fetchAccessToken(Form form) {
        return httpClient
                .request(Urls.FETCH_ACCESS_TOKEN)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .headers(NordeaSEConstants.NORDEA_CUSTOM_HEADERS)
                .body(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(ResultBankIdResponse.class);
    }

    public BankIdResponse formInitBankIdLogin(String ssn) {
        Form formBuilder = new Form(NordeaSEConstants.DEFAULT_FORM_PARAMS);
        formBuilder.put(NordeaSEConstants.FormParams.USERNAME, ssn);

        return sendBankIdRequest(formBuilder);
    }

    public FetchIdentityDataResponse fetchIdentityData() {
        final RequestBuilder request =
                httpClient
                        .request(Urls.FETCH_IDENTITY_DATA)
                        .headers(NordeaSEConstants.NORDEA_CUSTOM_HEADERS)
                        .accept(MediaType.APPLICATION_JSON_TYPE);
        return requestRefreshableGet(request, FetchIdentityDataResponse.class);
    }

    private <T> T requestRefreshableGet(RequestBuilder request, Class<T> responseType) {
        try {
            return request.header(
                            HttpHeaders.AUTHORIZATION, getTokenType() + ' ' + getAccessToken())
                    .header(HttpHeaders.ACCEPT_LANGUAGE, NordeaSEConstants.HeaderParams.LANGUAGE)
                    .header(Headers.REFERER, HeaderParams.REFERER_VALUE)
                    .get(responseType);

        } catch (HttpResponseException hre) {
            return handleRefreshableRequestAndErrors(hre, request, responseType, HttpMethod.GET);
        }
    }

    private String getTokenType() {
        return sessionStorage.get(NordeaSEConstants.StorageKeys.TOKEN_TYPE);
    }

    private String getAccessToken() {
        return sessionStorage.get(NordeaSEConstants.StorageKeys.ACCESS_TOKEN);
    }

    private String getRefreshToken() {
        return sessionStorage.get(NordeaSEConstants.StorageKeys.REFRESH_TOKEN);
    }

    private <T> T handleRefreshableRequestAndErrors(
            HttpResponseException hre,
            RequestBuilder request,
            Class<T> responseType,
            HttpMethod method) {
        ErrorResponse error = ErrorResponse.of(hre);
        if (!error.needsToRefreshToken()) {
            error.throwAppropriateErrorIfAny();
            throw hre;
        }

        tryRefreshAccessToken();
        // use the new access token
        request.overrideHeader(HttpHeaders.AUTHORIZATION, getTokenType() + ' ' + getAccessToken());
        switch (method) {
            case GET:
                return request.get(responseType);
            case POST:
                return request.post(responseType);
            case DELETE:
                return request.delete(responseType);
            case PUT:
                return request.put(responseType);
            case PATCH:
                return request.patch(responseType);
            default:
                throw new IllegalStateException("Unexpected HTTP method: " + method.name());
        }
    }

    public ResultBankIdResponse formPollBankIdLogin(BankIdResponse response, String ssn) {
        Form formBuilder = new Form(NordeaSEConstants.DEFAULT_FORM_PARAMS);
        formBuilder.put(NordeaSEConstants.FormParams.CODE, response.getCode());
        formBuilder.put(NordeaSEConstants.FormParams.USERNAME, ssn);

        return getBankIdAccessToken(formBuilder);
    }

    private BankIdResponse sendBankIdRequest(Form form) {
        return httpClient
                .request(NordeaSEConstants.Urls.LOGIN_BANKID)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(form, MediaType.APPLICATION_FORM_URLENCODED)
                .post(BankIdResponse.class);
    }

    private void tryRefreshAccessToken() {
        refreshAccessToken(getRefreshToken());
    }

    private void refreshAccessToken(String refreshToken) {

        refreshBankIdAccessToken(refreshToken);
    }

    private void refreshBankIdAccessToken(String refreshToken) {
        Form form = new Form(NordeaSEConstants.REFRESH_TOKEN_FORM);
        form.put(
                NordeaSEConstants.FormParams.GRANT_TYPE,
                NordeaSEConstants.StorageKeys.REFRESH_TOKEN);
        form.put(NordeaSEConstants.StorageKeys.REFRESH_TOKEN, refreshToken);

        fetchAccessToken(form).storeTokens(sessionStorage);
    }

    private ResultBankIdResponse getBankIdAccessToken(Form form) {
        return httpClient
                .request(NordeaSEConstants.Urls.LOGIN_BANKID)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(form, MediaType.APPLICATION_FORM_URLENCODED)
                .post(ResultBankIdResponse.class);
    }

    public InitBankIdResponse initBankId(InitBankIdRequest initiBankIdRequest) {
        return httpClient
                .request(Urls.INIT_BANKID)
                .headers(NordeaSEConstants.NORDEA_CUSTOM_HEADERS)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.WILDCARD_TYPE)
                .post(InitBankIdResponse.class, initiBankIdRequest);
    }

    public ResultBankIdResponse resultBankId(String reference) {
        return httpClient
                .request(Urls.POLL_BANKID + reference)
                .headers(NordeaSEConstants.NORDEA_CUSTOM_HEADERS)
                .header(Headers.SECURITY_TOKEN, sessionStorage.get(StorageKeys.SECURITY_TOKEN))
                .accept(MediaType.WILDCARD_TYPE)
                .get(ResultBankIdResponse.class);
    }

    public FetchTokenResponse fetchToken(FetchTokenRequest fetchTokenRequest) {
        return httpClient
                .request(Urls.FETCH_TOKEN)
                .headers(NordeaSEConstants.NORDEA_CUSTOM_HEADERS)
                .header(Headers.SECURITY_TOKEN, sessionStorage.get(StorageKeys.SECURITY_TOKEN))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.WILDCARD_TYPE)
                .post(FetchTokenResponse.class, fetchTokenRequest);
    }

    public FetchAccountResponse fetchAccount() {
        final RequestBuilder request =
                httpClient
                        .request(NordeaSEConstants.Urls.FETCH_ACCOUNTS)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshableGet(request, FetchAccountResponse.class);
    }

    public FetchAccountTransactionResponse fetchAccountTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        final URL url =
                NordeaSEConstants.Urls.FETCH_ACCOUNT_TRANSACTIONS
                        .parameter(IdTags.ACCOUNT_NUMBER, account.getApiIdentifier())
                        .queryParam(
                                QueryParams.START_DATE,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                        .queryParam(
                                QueryParams.END_DATE,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate));

        final RequestBuilder request =
                httpClient.request(url).accept(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshableGet(request, FetchAccountTransactionResponse.class);
    }
}
