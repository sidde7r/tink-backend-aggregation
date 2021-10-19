package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.Fetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.QueryParam;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.rpc.AutoStartResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.rpc.CreateSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.rpc.InitTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.rpc.OAuth2TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.entities.Form;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.rpc.PaymentCompleteResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.rpc.PaymentInitSignResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.rpc.PaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.rpc.PaymentSignStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.rpc.PaymentSourceAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.creditcard.rpc.FetchCreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.identity.rpc.IdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.rpc.FetchInvestmentAccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.rpc.FetchInvestmentHoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.rpc.FetchInvestmentsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.rpc.PensionFundsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transactionalaccount.rpc.FetchAccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.rpc.FetchPaymentsResponse;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.redirect.DenyAllRedirectHandler;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SkandiaBankenApiClient {

    private final TinkHttpClient httpClient;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final LogMasker logMasker;
    private final BankServiceInternalErrorFilter bankServiceInternalErrorFilter;

    private FetchAccountResponse cachedAccountsResponse;

    public SkandiaBankenApiClient(
            TinkHttpClient httpClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            LogMasker logMasker) {
        this.httpClient = httpClient;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.logMasker = logMasker;

        this.bankServiceInternalErrorFilter = new BankServiceInternalErrorFilter();
        addBankServiceInternalErrorFilter();
    }

    public InitTokenResponse fetchInitAccessToken() {
        final Form formBuilder = new Form();
        formBuilder.put(FormKeys.CLIENT_SECRET, FormValues.CLIENT_SECRET);
        formBuilder.put(FormKeys.CLIENT_ID, FormValues.CLIENT_ID_MEDIUM);
        formBuilder.put(FormKeys.GRANT_TYPE, FormValues.GRANT_TYPE);
        formBuilder.put(FormKeys.SCOPE, FormValues.SCOPE);

        return getRequestWithCommonHeaders(Urls.INIT_TOKEN)
                .body(formBuilder, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(InitTokenResponse.class);
    }

    public void createSession(CreateSessionRequest createSessionRequest) {
        getRequestWithCommonHeaders(Urls.CREATE_SESSION)
                .header(HeaderKeys.AUTHORIZATION, sessionStorage.get(StorageKeys.INIT_ACCESS_TOKEN))
                .type(MediaType.APPLICATION_JSON)
                .post(createSessionRequest);
    }

    public String extractRequestVerificationToken(String codeVerifier) {
        final String codeChallenge = calculateCodeChallenge(codeVerifier);
        final String randomState = UUID.randomUUID().toString().toUpperCase();
        logMasker.addNewSensitiveValueToMasker(codeChallenge);

        return httpClient
                .request(Urls.OAUTH_AUTHORIZE)
                .queryParam(QueryParam.CODE_CHALLENGE_METHOD, QueryParam.CODE_CHALLENGE_METHOD_S256)
                .queryParam(QueryParam.RESPONSE_TYPE, QueryParam.RESPONSE_TYPE_CODE)
                .queryParam(QueryParam.CLIENT_ID, QueryParam.CLIENT_ID_VALUE)
                .queryParam(QueryParam.CODE_CHALLENGE, codeChallenge)
                .queryParam(QueryParam.REDRIECT_URI, QueryParam.REDIRECT_URI_VALUE)
                .queryParam(QueryParam.PFIDP_ADAPTER_ID, QueryParam.PFIDP_ADAPTER_ID_MOBILE)
                .queryParam(QueryParam.SCOPE, QueryParam.SCOPES_READ)
                .queryParam(QueryParam.STATE, randomState)
                .get(String.class);
    }

    private String calculateCodeChallenge(String verifier) {
        final byte[] digest = Hash.sha256(verifier);
        return EncodingUtils.encodeAsBase64UrlSafe(digest);
    }

    public AutoStartResponse autoStartAuthenticate(String token) {
        final Form formBuilder = new Form();
        formBuilder.put(FormKeys.REQUEST_TOKEN, token);
        formBuilder.put(FormKeys.REQUEST_WITH, FormValues.REQUEST_WITH);

        return httpClient
                .request(Urls.OAUTH_AUTOSTART_AUTHORIZE)
                .body(formBuilder, MediaType.APPLICATION_FORM_URLENCODED)
                .post(AutoStartResponse.class);
    }

    public String autoStartAuthenticateRedirect() {
        return httpClient.request(Urls.OAUTH_CHOOSER_AUTHORIZE).get(String.class);
    }

    public BankIdResponse collectBankId(String token) {
        final Form formBuilder = new Form();
        formBuilder.put(FormKeys.REQUEST_TOKEN, token);
        formBuilder.put(FormKeys.REQUEST_WITH, FormValues.REQUEST_WITH);

        return httpClient
                .request(Urls.BANKID_COLLECT)
                .body(formBuilder, MediaType.APPLICATION_FORM_URLENCODED)
                .post(BankIdResponse.class);
    }

    public HttpResponse fetchCode(String redirect) {
        // Required since the following request leads to a redirect to the skandia bank app
        httpClient.addRedirectHandler(new DenyAllRedirectHandler());
        return httpClient.request(new URL(redirect)).get(HttpResponse.class);
    }

    public OAuth2TokenResponse fetchAuthToken(String code) {
        final Form formBuilder = new Form();
        formBuilder.put(FormKeys.CODE, code);
        formBuilder.put(FormKeys.CODE_VERIFIER, sessionStorage.get(StorageKeys.CODE_VERIFIER));
        formBuilder.put(FormKeys.CLIENT_ID, FormValues.CLIENT_ID_SHORT);
        formBuilder.put(FormKeys.REDIRECT_URI, FormValues.REDIRECT_URI);
        formBuilder.put(FormKeys.GRANT_TYPE, FormValues.GRANT_TYPE_FOR_BEARER);
        formBuilder.put(FormKeys.CLIENT_SECRET, FormValues.CLIENT_SECRET_FOR_BEARER);

        return getRequestWithCommonHeaders(Urls.FETCH_AUTH_TOKEN)
                .header(HeaderKeys.AUTHORIZATION, sessionStorage.get(StorageKeys.INIT_ACCESS_TOKEN))
                .body(formBuilder, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(OAuth2TokenResponse.class);
    }

    public FetchAccountResponse fetchAccounts() {
        if (cachedAccountsResponse == null) {
            cachedAccountsResponse =
                    getRequestWithTokenAndCommonHeaders(Urls.FETCH_ACCOUNTS)
                            .get(FetchAccountResponse.class);
        }

        return cachedAccountsResponse;
    }

    public FetchAccountTransactionsResponse fetchAccountTransactions(
            String accountId, String page) {
        return getRequestWithTokenAndCommonHeaders(
                        Urls.FETCH_ACCOUNT_TRANSACTIONS
                                .parameter(IdTags.ACCOUNT_ID, accountId)
                                .parameter(IdTags.PAGE, page)
                                .parameter(
                                        IdTags.BATCH_SIZE,
                                        Integer.toString(Fetcher.TRANSACTIONS_PER_BATCH)))
                .get(FetchAccountTransactionsResponse.class);
    }

    public FetchAccountTransactionsResponse fetchPendingAccountTransactions(String accountId) {
        return getRequestWithTokenAndCommonHeaders(
                        Urls.FETCH_PENDING_ACCOUNT_TRANSACTIONS.parameter(
                                IdTags.ACCOUNT_ID, accountId))
                .get(FetchAccountTransactionsResponse.class);
    }

    public void submitPayment(PaymentRequest paymentRequest) {
        getRequestWithTokenAndCommonHeaders(Urls.UNAPPROVED_PAYMENTS)
                .type(MediaType.APPLICATION_JSON)
                .post(new PaymentRequest[] {paymentRequest});
    }

    public FetchPaymentsResponse fetchUnapprovedPayments() {
        return getRequestWithTokenAndCommonHeaders(Urls.UNAPPROVED_PAYMENTS)
                .get(FetchPaymentsResponse.class);
    }

    public PaymentInitSignResponse initSignPayment(String encryptedPaymentId) {
        try {
            return getRequestWithTokenAndCommonHeaders(Urls.APPROVED_PAYMENTS)
                    .type(MediaType.APPLICATION_JSON)
                    .post(PaymentInitSignResponse.class, new String[] {encryptedPaymentId});
        } catch (HttpResponseException exception) {
            HttpResponse response = exception.getResponse();
            if (response.getStatus() == HttpStatus.SC_FORBIDDEN) {
                return response.getBody(PaymentInitSignResponse.class);
            }
            throw exception;
        }
    }

    public PaymentCompleteResponse completePayment(
            String encryptedPaymentId, String signReference) {
        return getRequestWithTokenAndCommonHeaders(Urls.APPROVED_PAYMENTS)
                .header(HeaderKeys.SIGNING_REFERENCE, signReference)
                .type(MediaType.APPLICATION_JSON)
                .post(PaymentCompleteResponse.class, new String[] {encryptedPaymentId});
    }

    public BankIdStatus pollPaymentSignStatus(String signReference) {
        return getRequestWithTokenAndCommonHeaders(
                        Urls.POLL_SIGNING_PAYMENTS.parameter(IdTags.SIGN_REFERENCE, signReference))
                .get(PaymentSignStatusResponse.class)
                .getBankIdStatus();
    }

    public void deleteUnapprovedPayment(String encryptedPaymentId) {
        getRequestWithTokenAndCommonHeaders(
                        Urls.DELETE_UNAPPROVED_PAYMENT.parameter(
                                IdTags.ENCRYPTED_PAYMENT_ID, encryptedPaymentId))
                .delete();
    }

    public PaymentSourceAccountsResponse fetchPaymentSourceAccounts() {
        return getRequestWithTokenAndCommonHeaders(Urls.PAYMENT_SOURCE_ACCOUNTS)
                .get(PaymentSourceAccountsResponse.class);
    }

    public FetchPaymentsResponse fetchApprovedPayments() {
        return getRequestWithTokenAndCommonHeaders(Urls.FETCH_APPROVED_PAYMENTS)
                .get(FetchPaymentsResponse.class);
    }

    public FetchCreditCardsResponse fetchCards() {
        return getRequestWithTokenAndCommonHeaders(Urls.FETCH_CARDS)
                .get(FetchCreditCardsResponse.class);
    }

    public FetchInvestmentsResponse fetchInvestments() {
        return getRequestWithTokenAndCommonHeaders(Urls.FETCH_INVESTMENT_ACCOUNTS)
                .get(FetchInvestmentsResponse.class);
    }

    public FetchInvestmentAccountDetailsResponse fetchInvestmentAccountDetails(
            String investmentAccountNumber) {
        return getRequestWithTokenAndCommonHeaders(
                        Urls.FETCH_INVESTMENT_ACCOUNT_DETAILS.parameter(
                                IdTags.ACCOUNT_ID, investmentAccountNumber))
                .get(FetchInvestmentAccountDetailsResponse.class);
    }

    public FetchInvestmentHoldingsResponse fetchHoldings(String investmentAccountNumber) {
        return getRequestWithTokenAndCommonHeaders(
                        Urls.FETCH_INVESTMENT_HOLDINGS.parameter(
                                IdTags.ACCOUNT_ID, investmentAccountNumber))
                .get(FetchInvestmentHoldingsResponse.class);
    }

    public PensionFundsResponse fetchPensionHoldings(
            String partNumber, String encrypedNationalIdentificationNumber) {
        return getRequestWithTokenAndCommonHeaders(
                        Urls.FETCH_PENSIONS_HOLDINGS
                                .parameter(IdTags.PART_ID, partNumber)
                                .queryParam(
                                        QueryParam.ENCRYPED_NATIONAL_IDENTIFICATION_NUMBER,
                                        encrypedNationalIdentificationNumber))
                .get(PensionFundsResponse.class);
    }

    public IdentityDataResponse fetchIdentityData() {
        return getRequestWithTokenAndCommonHeaders(Urls.FETCH_IDENTITY)
                .get(IdentityDataResponse.class);
    }

    public OAuth2TokenResponse refreshToken(String refreshToken) {
        final Form form = new Form();
        form.put(FormKeys.REFRESH_TOKEN, refreshToken);
        form.put(FormKeys.CLIENT_SECRET, FormValues.CLIENT_SECRET_FOR_BEARER);
        form.put(FormKeys.CLIENT_ID, FormValues.CLIENT_ID_SHORT);
        form.put(FormKeys.GRANT_TYPE, FormValues.REFRESH_TOKEN);

        return getRequestWithCommonHeaders(Urls.FETCH_AUTH_TOKEN)
                .header(HeaderKeys.AUTHORIZATION, sessionStorage.get(StorageKeys.INIT_ACCESS_TOKEN))
                .body(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(OAuth2TokenResponse.class);
    }

    private OAuth2Token getValidOAuth2Token() {
        OAuth2Token oAuth2Token =
                persistentStorage
                        .get(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                        .get();

        if (oAuth2Token.hasAccessExpired()) {
            oAuth2Token = refreshToken(oAuth2Token.getRefreshToken().get()).toOAuth2Token();
            persistentStorage.put(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, oAuth2Token);
        }

        return oAuth2Token;
    }

    public String fetchMessage() {
        return httpClient.request(Urls.LOGIN_MESSAGE).get(String.class);
    }

    public String fetchOtpChooser() {
        return httpClient.request(Urls.LOGIN_OTP_CHOOSER).get(String.class);
    }

    private RequestBuilder getRequestWithTokenAndCommonHeaders(URL url) {
        return getRequestWithCommonHeaders(url).addBearerToken(getValidOAuth2Token());
    }

    private RequestBuilder getRequestWithCommonHeaders(URL url) {
        return httpClient
                .request(url)
                .header(HeaderKeys.ADRUM_1, HeaderValues.ADRUM_1)
                .header(HeaderKeys.ADRUM, HeaderValues.ADRUM)
                .header(HeaderKeys.CLIENT_ID, HeaderValues.CLIENT_ID);
    }

    public void addBankServiceInternalErrorFilter() {
        httpClient.addFilter(bankServiceInternalErrorFilter);
    }

    public void removeBankServiceInternalErrorFilter() {
        httpClient.removeFilter(bankServiceInternalErrorFilter);
    }
}
