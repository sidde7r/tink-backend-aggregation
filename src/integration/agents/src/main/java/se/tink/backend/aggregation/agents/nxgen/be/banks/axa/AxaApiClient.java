package se.tink.backend.aggregation.agents.nxgen.be.banks.axa;

import java.net.URI;
import java.security.KeyPair;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaConstants.Request;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.AnonymousInvokeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.AnonymousInvokeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.AssertFormRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.AssertFormResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.BaseRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.BindRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.BindResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.LogonResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.rpc.GetAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.rpc.GetTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.utils.AxaCryptoUtil;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class AxaApiClient {

    private final TinkHttpClient httpClient;
    private final AxaStorage storage;

    public AxaApiClient(TinkHttpClient httpClient, AxaStorage storage) {
        this.httpClient = httpClient;
        this.storage = storage;
    }

    public LogonResponse postLogon() {
        Form body =
                Form.builder()
                        .put("grant_type", "password")
                        .put("scope", "mobilebanking")
                        .put("username", storage.getParamsSessionId())
                        .put("jwt", storage.getToken())
                        .put("language", "nl")
                        .put("applCd", "MOBILEBANK")
                        .put("batchInstallationId", storage.getBatchInstallationId())
                        .build();

        return httpClient
                .request(AxaConstants.Url.LOGON)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        String.format("Basic %s", AxaConstants.Request.BASIC_AUTH))
                .headers(AxaConstants.HEADERS_FORM)
                .body(body.serialize())
                .post(LogonResponse.class);
    }

    public GetAccountsResponse postGetAccounts(
            final int customerId, final String accessToken, final String locale) {
        final GetAccountsRequest body =
                GetAccountsRequest.builder()
                        .setApplCd(AxaConstants.Request.APPL_CD)
                        .setLanguage(locale)
                        .setCustomerId(customerId)
                        .build();
        return httpClient
                .request(AxaConstants.Url.FETCH_ACCOUNTS)
                .headers(AxaConstants.HEADERS_JSON)
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken))
                .body(body)
                .post(GetAccountsResponse.class);
    }

    public GetTransactionsResponse postGetTransactions(
            final int customerId,
            final String accessToken,
            final String accountReferenceNumber,
            final String locale) {
        final GetTransactionsRequest body =
                GetTransactionsRequest.builder()
                        .setApplCd(AxaConstants.Request.APPL_CD)
                        .setLanguage(locale)
                        .setCustomerId(customerId)
                        .setAccountReferenceNumber(accountReferenceNumber)
                        .build();
        return httpClient
                .request(AxaConstants.Url.FETCH_TRANSACTIONS)
                .headers(AxaConstants.HEADERS_JSON)
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken))
                .body(body)
                .post(GetTransactionsResponse.class);
    }

    public AnonymousInvokeResponse anonymousInvoke(AnonymousInvokeRequest request) {
        return createRequest(AxaConstants.Url.ANONYMOUS_INVOKE, request)
                .post(AnonymousInvokeResponse.class);
    }

    public AssertFormResponse assertForm(AssertFormRequest request) {
        return createAssertRequest(request).post(AssertFormResponse.class);
    }

    public AssertFormResponse assertFormWithSignature(AssertFormRequest request) {
        String requestBody = request.toJson();
        URL url =
                new URL(Url.ASSERT)
                        .queryParam("did", storage.getDeviceIdFromHeader())
                        .queryParam("sid", storage.getSessionIdFromHeader());
        try {
            return createRequestWithSignature(url, requestBody).post(AssertFormResponse.class);
        } catch (HttpResponseException exception) {
            if (isSessionExpired(exception.getResponse())) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            throw exception;
        }
    }

    public BindResponse bind(BindRequest request) {
        return createRequest(AxaConstants.Url.BIND, request).post(BindResponse.class);
    }

    public LoginResponse login(LoginRequest request) {
        String requestBody = request.toJson();
        URL url = new URL(Url.LOGIN).queryParam("did", storage.getDeviceIdFromHeader());

        return createRequestWithSignature(url, requestBody).post(LoginResponse.class);
    }

    private RequestBuilder createAssertRequest(BaseRequest body) {
        URL url =
                new URL(Url.ASSERT)
                        .queryParam("did", storage.getDeviceIdFromHeader())
                        .queryParam("sid", storage.getSessionIdFromHeader());

        return createRequest(url, body);
    }

    private RequestBuilder createRequest(URL url, BaseRequest body) {
        return httpClient.request(url).headers(AxaConstants.AUTH_HEADERS_JSON).body(body);
    }

    private RequestBuilder createRequest(String url, BaseRequest body) {
        return httpClient.request(url).headers(AxaConstants.AUTH_HEADERS_JSON).body(body);
    }

    private RequestBuilder createRequestWithSignature(URL url, String body) {
        String contentSignatureValue = createContentSignature(url, body);
        return httpClient
                .request(url)
                .headers(AxaConstants.AUTH_HEADERS_JSON)
                .header("Content-Signature", contentSignatureValue)
                .body(body);
    }

    private String createContentSignature(URL url, String requestBody) {
        KeyPair keyPair = storage.getRequestSignatureECKeyPair();
        String pathWithQuery = extractPathWithQuery(url);
        String signature =
                AxaCryptoUtil.createHeaderSignature(
                        keyPair, pathWithQuery, Request.TS_CLIENT_VERSION, requestBody);
        return new StringBuilder()
                .append("data:")
                .append(signature)
                .append(";key-id:")
                .append(storage.getDeviceIdFromHeader())
                .append(";scheme:3")
                .toString();
    }

    private String extractPathWithQuery(URL url) {
        URI uri = url.toUri();
        String rawQuery = uri.getRawQuery();
        String result = uri.getPath();

        if (StringUtils.isNotBlank(rawQuery)) {
            result += "?" + rawQuery;
        }

        return result.replace("/AXA_BANK_TransmitApi", StringUtils.EMPTY);
    }

    private boolean isSessionExpired(HttpResponse response) {
        ErrorResponse responseBody = response.getBody(ErrorResponse.class);
        return response.getStatus() == 401
                && ErrorCodes.SESSION_REJECTED == responseBody.getErrorCode();
    }
}
