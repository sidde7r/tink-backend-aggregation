package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30;

import io.vavr.control.Option;
import java.util.Date;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.AuthMethod;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.FormParams;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.HeaderParams;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.AuthDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.AuthDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.BankIdAutostartResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.ConfirmEnrollmentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.ConfirmEnrollmentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.EnrollmentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.EnrollmentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.FetchCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.FetchCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.InitBankIdAutostartRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.InitDeviceAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.PasswordTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.PasswordTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.ResultBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.VerifyPersonalCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.VerifyPersonalCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.entities.Form;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.BankPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.CompleteTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.ConfirmTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.InternalBankTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.InternalBankTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.PaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.ResultSignResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.SignatureRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc.SignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.creditcard.rpc.FetchCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.creditcard.rpc.FetchCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.einvoice.entities.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.einvoice.rpc.FetchPaymentsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.identitydata.rpc.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.investment.rpc.FetchInvestmentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.rpc.FetchLoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.rpc.FetchLoanResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount.rpc.FetchAccountTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transfer.rpc.FetchBeneficiariesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class NordeaBaseApiClient {
    private final TinkHttpClient httpClient;
    private final SessionStorage sessionStorage;
    private final NordeaConfiguration nordeaConfiguration;

    public NordeaBaseApiClient(
            TinkHttpClient httpClient,
            SessionStorage sessionStorage,
            NordeaConfiguration nordeaConfiguration) {
        this.httpClient = httpClient;
        this.sessionStorage = sessionStorage;
        this.nordeaConfiguration = nordeaConfiguration;
    }

    public BankIdAutostartResponse initBankIdAutostart(
            InitBankIdAutostartRequest initBankIdAutostartRequest) {
        final RequestBuilder request =
                httpClient
                        .request(
                                Urls.getUrl(
                                        nordeaConfiguration.getBaseUrl(),
                                        Urls.LOGIN_BANKID_AUTOSTART))
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .body(initBankIdAutostartRequest, MediaType.APPLICATION_JSON_TYPE);

        if (nordeaConfiguration.isBusinessAgent()) {
            request.headers(NordeaBaseConstants.NORDEA_BUSINESS_HEADERS);
        }

        return request.post(BankIdAutostartResponse.class);
    }

    public BankIdAutostartResponse pollBankIdAutostart(String sessionId) {
        final RequestBuilder request =
                httpClient
                        .request(
                                Urls.getUrl(
                                        nordeaConfiguration.getBaseUrl(),
                                        Urls.LOGIN_BANKID_AUTOSTART.concat(sessionId)))
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        if (nordeaConfiguration.isBusinessAgent()) {
            request.headers(NordeaBaseConstants.NORDEA_BUSINESS_HEADERS);
        }

        return request.get(BankIdAutostartResponse.class);
    }

    public FetchCodeResponse fetchLoginCode(FetchCodeRequest fetchCodeRequest) {
        final RequestBuilder request =
                httpClient
                        .request(
                                Urls.getUrl(
                                        nordeaConfiguration.getBaseUrl(), Urls.FETCH_LOGIN_CODE))
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .body(fetchCodeRequest, MediaType.APPLICATION_JSON_TYPE);

        if (nordeaConfiguration.isBusinessAgent()) {
            request.headers(NordeaBaseConstants.NORDEA_BUSINESS_HEADERS);
        }

        return request.post(FetchCodeResponse.class);
    }

    public ResultBankIdResponse fetchAccessToken(String code, String codeVerifier) {
        Form form = new Form(NordeaBaseConstants.REQUEST_TOKEN_FORM);
        form.put(FormParams.CODE, code);
        form.put(FormParams.CODE_VERIFIER, codeVerifier);
        form.put(FormParams.CLIENT_ID, nordeaConfiguration.getClientId());
        form.put(FormParams.REDIRECT_URI, nordeaConfiguration.getRedirectUri());

        return fetchAccessToken(form);
    }

    private ResultBankIdResponse fetchAccessToken(Form form) {
        final RequestBuilder request =
                httpClient
                        .request(
                                Urls.getUrl(
                                        nordeaConfiguration.getBaseUrl(), Urls.FETCH_ACCESS_TOKEN))
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .body(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);

        if (nordeaConfiguration.isBusinessAgent()) {
            request.headers(NordeaBaseConstants.NORDEA_BUSINESS_HEADERS);
        }

        return request.post(ResultBankIdResponse.class);
    }

    private ResultBankIdResponse getBankIdAccessToken(Form form) {
        return httpClient
                .request(Urls.getUrl(nordeaConfiguration.getBaseUrl(), Urls.LOGIN_BANKID))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(form, MediaType.APPLICATION_FORM_URLENCODED)
                .post(ResultBankIdResponse.class);
    }

    public PasswordTokenResponse getPasswordAccessToken(Form request) {
        return httpClient
                .request(Urls.getUrl(nordeaConfiguration.getBaseUrl(), Urls.PASSWORD_TOKEN))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(request, MediaType.APPLICATION_FORM_URLENCODED)
                .post(PasswordTokenResponse.class);
    }

    public FetchAccountResponse fetchAccount() {
        final RequestBuilder request =
                httpClient
                        .request(Urls.getUrl(nordeaConfiguration.getBaseUrl(), Urls.FETCH_ACCOUNTS))
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshableGet(request, FetchAccountResponse.class);
    }

    public FetchAccountTransactionResponse fetchAccountTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        final URL url =
                Urls.getUrl(nordeaConfiguration.getBaseUrl(), Urls.FETCH_TRANSACTIONS)
                        .parameter(IdTags.ACCOUNT_NUMBER, account.getApiIdentifier())
                        .queryParam(
                                NordeaBaseConstants.PROUDCT_CODE,
                                account.getFromTemporaryStorage(NordeaBaseConstants.PROUDCT_CODE))
                        .queryParam(
                                QueryParams.START_DATE,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                        .queryParam(
                                QueryParams.END_DATE,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate));

        final RequestBuilder request =
                httpClient.request(url).accept(MediaType.APPLICATION_JSON_TYPE);

        final FetchAccountTransactionResponse accountTransactionResponse =
                requestRefreshableGet(request, FetchAccountTransactionResponse.class);
        accountTransactionResponse.setConfiguration(nordeaConfiguration);

        return accountTransactionResponse;
    }

    public FetchCardsResponse fetchCards() {
        final RequestBuilder request =
                httpClient
                        .request(Urls.getUrl(nordeaConfiguration.getBaseUrl(), Urls.FETCH_CARDS))
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshableGet(request, FetchCardsResponse.class);
    }

    public FetchCardTransactionsResponse fetchCardTransactions(int page, String accountId) {
        final URL url =
                Urls.getUrl(nordeaConfiguration.getBaseUrl(), Urls.FETCH_CARD_TRANSACTIONS)
                        .parameter(IdTags.CARD_ID, accountId)
                        .queryParam(QueryParams.PAGE, Integer.toString(page))
                        .queryParam(QueryParams.PAGE_SIZE, QueryParams.PAGE_SIZE_LIMIT);

        final RequestBuilder request =
                httpClient.request(url).accept(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshableGet(request, FetchCardTransactionsResponse.class);
    }

    public FetchInvestmentResponse fetchInvestments() {
        final RequestBuilder request =
                httpClient
                        .request(
                                Urls.getUrl(
                                        nordeaConfiguration.getBaseUrl(), Urls.FETCH_INVESTMENTS))
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshableGet(request, FetchInvestmentResponse.class);
    }

    public FetchLoanResponse fetchLoans() {
        final RequestBuilder request =
                httpClient.request(Urls.getUrl(nordeaConfiguration.getBaseUrl(), Urls.FETCH_LOANS));

        return requestRefreshableGet(request, FetchLoanResponse.class);
    }

    public FetchLoanDetailsResponse fetchLoanDetails(String accountId) {
        final RequestBuilder request =
                httpClient.request(
                        Urls.getUrl(nordeaConfiguration.getBaseUrl(), Urls.FETCH_LOAN_DETAILS)
                                .parameter(IdTags.LOAN_ID, accountId));

        return requestRefreshableGet(request, FetchLoanDetailsResponse.class);
    }

    public FetchIdentityDataResponse fetchIdentityData() {
        final RequestBuilder request =
                httpClient
                        .request(
                                Urls.getUrl(
                                        nordeaConfiguration.getBaseUrl(), Urls.FETCH_IDENTITY_DATA))
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        if (nordeaConfiguration.isBusinessAgent()) {
            request.headers(NordeaBaseConstants.NORDEA_BUSINESS_HEADERS);
        }

        return requestRefreshableGet(request, FetchIdentityDataResponse.class);
    }

    public FetchPaymentsResponse fetchPayments() {
        final URL url =
                Urls.getUrl(nordeaConfiguration.getBaseUrl(), Urls.FETCH_PAYMENTS)
                        .queryParam(QueryParams.STATUS, QueryParams.STATUS_VALUES);

        final RequestBuilder request =
                httpClient
                        .request(url)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .type(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshableGet(request, FetchPaymentsResponse.class);
    }

    public PaymentEntity fetchPaymentDetails(String paymentId) {
        final RequestBuilder request =
                httpClient
                        .request(
                                Urls.getUrl(
                                                nordeaConfiguration.getBaseUrl(),
                                                Urls.FETCH_PAYMENTS_DETAILS)
                                        .parameter(IdTags.PAYMENT_ID, paymentId))
                        .accept(MediaType.APPLICATION_JSON_TYPE);
        try {
            return requestRefreshableGet(request, PaymentEntity.class);
        } catch (HttpResponseException hre) {
            ErrorResponse error = ErrorResponse.of(hre);
            error.throwAppropriateErrorIfAny();

            throw hre;
        }
    }

    public FetchBeneficiariesResponse fetchBeneficiaries() {
        final RequestBuilder request =
                httpClient
                        .request(
                                Urls.getUrl(
                                        nordeaConfiguration.getBaseUrl(), Urls.FETCH_BENEFICIARIES))
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshableGet(request, FetchBeneficiariesResponse.class);
    }

    public InternalBankTransferResponse executeInternalBankTransfer(
            InternalBankTransferRequest transferRequest) {
        final RequestBuilder request =
                httpClient
                        .request(Urls.getUrl(nordeaConfiguration.getBaseUrl(), Urls.FETCH_PAYMENTS))
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .body(transferRequest, MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshablePost(request, InternalBankTransferResponse.class);
    }

    public BankPaymentResponse executeBankPayment(PaymentRequest transferRequest) {
        final RequestBuilder request =
                httpClient
                        .request(Urls.getUrl(nordeaConfiguration.getBaseUrl(), Urls.FETCH_PAYMENTS))
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .body(transferRequest, MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshablePost(request, BankPaymentResponse.class);
    }

    public PaymentEntity updatePayment(PaymentRequest updateRequest) {
        final RequestBuilder request =
                httpClient
                        .request(
                                Urls.getUrl(
                                                nordeaConfiguration.getBaseUrl(),
                                                Urls.FETCH_PAYMENTS_DETAILS)
                                        .parameter(
                                                IdTags.PAYMENT_ID,
                                                updateRequest.getApiIdentifier()))
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .body(updateRequest, MediaType.APPLICATION_JSON_TYPE);
        return requestRefreshablePut(request, PaymentEntity.class);
    }

    public ConfirmTransferResponse confirmBankTransfer(
            ConfirmTransferRequest confirmTransferRequest) {
        final RequestBuilder request =
                httpClient
                        .request(
                                Urls.getUrl(
                                        nordeaConfiguration.getBaseUrl(), Urls.CONFIRM_TRANSFER))
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .body(confirmTransferRequest, MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshablePost(request, ConfirmTransferResponse.class);
    }

    public SignatureResponse signTransfer(SignatureRequest signatureRequest) {
        final RequestBuilder request =
                httpClient
                        .request(Urls.getUrl(nordeaConfiguration.getBaseUrl(), Urls.SIGN_TRANSFER))
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .body(signatureRequest, MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshablePost(request, SignatureResponse.class);
    }

    public ResultSignResponse pollSign(String orderRef, int pollSequence) {
        final URL url =
                Urls.getUrl(nordeaConfiguration.getBaseUrl(), Urls.POLL_SIGN)
                        .parameter(IdTags.ORDER_REF, orderRef)
                        .queryParam(QueryParams.POLLING_SEQUENCE, Integer.toString(pollSequence));

        final RequestBuilder request =
                httpClient
                        .request(url)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshableGet(request, ResultSignResponse.class);
    }

    public CompleteTransferResponse completeTransfer(String orderRef) {
        final RequestBuilder request =
                httpClient
                        .request(
                                Urls.getUrl(
                                                nordeaConfiguration.getBaseUrl(),
                                                Urls.COMPLETE_TRANSFER)
                                        .parameter(IdTags.ORDER_REF, orderRef))
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .type(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshablePost(request, CompleteTransferResponse.class);
    }

    public void keepAlive() throws SessionException {
        try {
            Option.of(getRefreshToken())
                    .peek(this::refreshAccessToken)
                    .getOrElseThrow(SessionError.SESSION_EXPIRED::exception);
        } catch (HttpResponseException hre) {
            ErrorResponse error = ErrorResponse.of(hre);
            if (error.isInvalidRefreshToken()) {
                throw SessionError.SESSION_EXPIRED.exception(hre);
            }
            throw hre;
        }
    }
    // Nordeas short lived access tokens requires us to sometimes have to refresh the
    // access token during a request. This method should be used by all data fetching calls

    private <T> T requestRefreshableGet(RequestBuilder request, Class<T> responseType) {
        try {
            final RequestBuilder requestBuilder =
                    request.header(
                                    HttpHeaders.AUTHORIZATION,
                                    getTokenType() + ' ' + getAccessToken())
                            .header(HttpHeaders.ACCEPT_LANGUAGE, HeaderParams.LANGUAGE);
            if (nordeaConfiguration.isBusinessAgent()) {
                request.header(Headers.REFERER, HeaderParams.BUSINESS_REFERER_VALUE);
            }
            return requestBuilder.get(responseType);

        } catch (HttpResponseException hre) {
            return handleRefreshableRequestAndErrors(hre, request, responseType, HttpMethod.GET);
        }
    }

    private <T> T requestRefreshablePost(RequestBuilder request, Class<T> responseType) {
        try {
            return request.header(
                            HttpHeaders.AUTHORIZATION, getTokenType() + ' ' + getAccessToken())
                    .header(HttpHeaders.ACCEPT_LANGUAGE, HeaderParams.LANGUAGE)
                    .post(responseType);

        } catch (HttpResponseException hre) {
            return handleRefreshableRequestAndErrors(hre, request, responseType, HttpMethod.POST);
        }
    }

    private <T> T requestRefreshablePut(RequestBuilder request, Class<T> responseType) {
        try {
            return request.header(
                            HttpHeaders.AUTHORIZATION, getTokenType() + ' ' + getAccessToken())
                    .header(HttpHeaders.ACCEPT_LANGUAGE, HeaderParams.LANGUAGE)
                    .put(responseType);

        } catch (HttpResponseException hre) {
            return handleRefreshableRequestAndErrors(hre, request, responseType, HttpMethod.PUT);
        }
    }

    private <T> T requestRefreshablePatch(RequestBuilder request, Class<T> responseType) {
        try {
            return request.header(
                            HttpHeaders.AUTHORIZATION, getTokenType() + ' ' + getAccessToken())
                    .header(HttpHeaders.ACCEPT_LANGUAGE, HeaderParams.LANGUAGE)
                    .patch(responseType);

        } catch (HttpResponseException hre) {
            return handleRefreshableRequestAndErrors(hre, request, responseType, HttpMethod.PATCH);
        }
    }

    private <T> T requestRefreshableDelete(RequestBuilder request, Class<T> responseType) {
        try {
            return request.header(
                            HttpHeaders.AUTHORIZATION, getTokenType() + ' ' + getAccessToken())
                    .header(HttpHeaders.ACCEPT_LANGUAGE, HeaderParams.LANGUAGE)
                    .delete(responseType);

        } catch (HttpResponseException hre) {
            return handleRefreshableRequestAndErrors(hre, request, responseType, HttpMethod.DELETE);
        }
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

    private void tryRefreshAccessToken() {
        refreshAccessToken(getRefreshToken());
    }

    private void refreshAccessToken(String refreshToken) {
        String tokenAuthMethod = sessionStorage.get(StorageKeys.TOKEN_AUTH_METHOD);
        if (AuthMethod.NASA.equals(tokenAuthMethod)) {
            refreshPasswordAccessToken(refreshToken);
        } else {
            refreshBankIdAccessToken(refreshToken);
        }
    }

    private void refreshPasswordAccessToken(String refreshToken) {
        PasswordTokenResponse response =
                getPasswordAccessToken(PasswordTokenRequest.of(refreshToken));
        response.storeTokens(sessionStorage);
    }

    private void refreshBankIdAccessToken(String refreshToken) {
        Form form = new Form(NordeaBaseConstants.REFRESH_TOKEN_FORM);
        form.put(FormParams.GRANT_TYPE, StorageKeys.REFRESH_TOKEN);
        form.put(StorageKeys.REFRESH_TOKEN, refreshToken);

        fetchAccessToken(form).storeTokens(sessionStorage);
    }

    public void logout() {
        String tokenAuthMethod = sessionStorage.get(StorageKeys.TOKEN_AUTH_METHOD);
        URL url =
                AuthMethod.NASA.equals(tokenAuthMethod)
                        ? Urls.getUrl(nordeaConfiguration.getBaseUrl(), Urls.LOGIN_PASSWORD)
                        : Urls.getUrl(nordeaConfiguration.getBaseUrl(), Urls.LOGOUT_BANKID);
        Form formBuilder = new Form();
        formBuilder.put(FormParams.TOKEN, getAccessToken());
        formBuilder.put(FormParams.TOKEN_TYPE_HINT, FormParams.TOKEN_TYPE);

        httpClient.request(url).body(formBuilder, MediaType.APPLICATION_FORM_URLENCODED).post();
    }

    private String getTokenType() {
        return sessionStorage.get(StorageKeys.TOKEN_TYPE);
    }

    private String getAccessToken() {
        return sessionStorage.get(StorageKeys.ACCESS_TOKEN);
    }

    private String getRefreshToken() {
        return sessionStorage.get(StorageKeys.REFRESH_TOKEN);
    }

    public EnrollmentResponse enrollForPersonalCode(EnrollmentRequest enrollmentRequest) {
        final RequestBuilder request =
                httpClient
                        .request(
                                Urls.getUrl(nordeaConfiguration.getBaseUrl(), Urls.ENROLL)
                                        .parameter(
                                                IdTags.APPLICATION_ID,
                                                nordeaConfiguration.getClientId()))
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .body(enrollmentRequest, MediaType.APPLICATION_JSON_TYPE);
        return requestRefreshablePost(request, EnrollmentResponse.class);
    }

    public ConfirmEnrollmentResponse confirmEnrollment(
            ConfirmEnrollmentRequest confirmEnrollmentRequest, String id) {
        final RequestBuilder request =
                httpClient
                        .request(
                                Urls.getUrl(
                                                nordeaConfiguration.getBaseUrl(),
                                                Urls.CONFIRM_ENROLLMENT)
                                        .parameter(
                                                IdTags.APPLICATION_ID,
                                                nordeaConfiguration.getClientId())
                                        .parameter(IdTags.ENROLLMENT_ID, id))
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .body(confirmEnrollmentRequest, MediaType.APPLICATION_JSON_TYPE);
        return requestRefreshablePatch(request, ConfirmEnrollmentResponse.class);
    }

    public InitDeviceAuthResponse initDeviceAuthentication(String enrollmentId) {
        final RequestBuilder request =
                httpClient
                        .request(
                                Urls.getUrl(nordeaConfiguration.getBaseUrl(), Urls.INIT_DEVICE_AUTH)
                                        .parameter(
                                                IdTags.APPLICATION_ID,
                                                nordeaConfiguration.getClientId())
                                        .parameter(IdTags.ENROLLMENT_ID, enrollmentId))
                        .accept(MediaType.APPLICATION_JSON_TYPE);
        return requestRefreshablePost(request, InitDeviceAuthResponse.class);
    }

    public AuthDeviceResponse authenticateDevice(
            AuthDeviceRequest authDeviceRequest, String enrollmentId) {
        final RequestBuilder request =
                httpClient
                        .request(
                                Urls.getUrl(
                                                nordeaConfiguration.getBaseUrl(),
                                                Urls.COMPLETE_DEVICE_AUTH)
                                        .parameter(
                                                IdTags.APPLICATION_ID,
                                                nordeaConfiguration.getClientId())
                                        .parameter(IdTags.ENROLLMENT_ID, enrollmentId))
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .body(authDeviceRequest, MediaType.APPLICATION_JSON_TYPE);
        return requestRefreshablePost(request, AuthDeviceResponse.class);
    }

    public VerifyPersonalCodeResponse verifyPersonalCode(VerifyPersonalCodeRequest verifyRequest) {
        return httpClient
                .request(Urls.getUrl(nordeaConfiguration.getBaseUrl(), Urls.VERIFY_PERSONAL_CODE))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(verifyRequest, MediaType.APPLICATION_JSON_TYPE)
                .put(VerifyPersonalCodeResponse.class, verifyRequest);
    }

    public ResultSignResponse cancelSign(String orderRef) {
        final RequestBuilder request =
                httpClient
                        .request(
                                Urls.getUrl(nordeaConfiguration.getBaseUrl(), Urls.POLL_SIGN)
                                        .parameter(IdTags.ORDER_REF, orderRef))
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshableDelete(request, ResultSignResponse.class);
    }
}
