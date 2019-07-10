package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken;

import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.QueryParam.CLIENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.QueryParam.CLIENT_ID_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.QueryParam.CODE_CHALLENGE;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.QueryParam.CODE_CHALLENGE_METHOD;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.QueryParam.CODE_CHALLENGE_METHOD_S256;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.QueryParam.ENCRYPED_NATIONAL_IDENTIFICATION_NUMBER;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.QueryParam.PFIDP_ADAPTER_ID;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.QueryParam.PFIDP_ADAPTER_ID_MOBILE;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.QueryParam.REDIRECT_URI_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.QueryParam.REDRIECT_URI;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.QueryParam.RESPONSE_TYPE;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.QueryParam.RESPONSE_TYPE_CODE;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.QueryParam.SCOPE;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.QueryParam.SCOPES_READ;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.QueryParam.STATE;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.rpc.AutoStartResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.rpc.BearerTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.rpc.CreateSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.rpc.InitTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.entities.Form;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.creditcard.rpc.FetchCreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.identity.rpc.IdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.rpc.FetchInvestmentAccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.rpc.FetchInvestmentHoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.rpc.FetchInvestmentsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.rpc.PensionFundsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transactionalaccount.rpc.FetchAccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.rpc.FetchApprovedPaymentsResponse;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.redirect.DenyAllRedirectHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SkandiaBankenApiClient {
    private final TinkHttpClient httpClient;
    private final SessionStorage sessionStorage;

    public SkandiaBankenApiClient(TinkHttpClient httpClient, SessionStorage sessionStorage) {
        this.httpClient = httpClient;
        this.sessionStorage = sessionStorage;
    }

    public InitTokenResponse fetchInitAccessToken() {
        final Form formBuilder = new Form();
        formBuilder.put(FormKeys.CLIENT_SECRET, FormValues.CLIENT_SECRET);
        formBuilder.put(FormKeys.CLIENT_ID, FormValues.CLIENT_ID_MEDIUM);
        formBuilder.put(FormKeys.GRANT_TYPE, FormValues.GRANT_TYPE);
        formBuilder.put(FormKeys.SCOPE, FormValues.SCOPE);

        return httpClient
                .request(Urls.INIT_TOKEN)
                .header(HeaderKeys.ADRUM_1, HeaderValues.ADRUM_1)
                .header(HeaderKeys.ADRUM, HeaderValues.ADRUM)
                .header(HeaderKeys.SK_API_KEY, HeaderValues.SK_API_KEY)
                .body(formBuilder, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(InitTokenResponse.class);
    }

    public void createSession(CreateSessionRequest createSessionRequest) {
        httpClient
                .request(Urls.CREATE_SESSION)
                .header(HeaderKeys.ADRUM_1, HeaderValues.ADRUM_1)
                .header(HeaderKeys.AUTHORIZATION, sessionStorage.get(StorageKeys.INIT_ACCESS_TOKEN))
                .header(HeaderKeys.ADRUM, HeaderValues.ADRUM)
                .header(HeaderKeys.SK_API_KEY, HeaderValues.SK_API_KEY)
                .type(MediaType.APPLICATION_JSON)
                .post(createSessionRequest);
    }

    public String extractRequestVerificationToken(String codeVerifier) {
        final String codeChallenge = calculateCodeChallenge(codeVerifier);
        final String randomState = UUID.randomUUID().toString().toUpperCase();

        return httpClient
                .request(Urls.OAUTH_AUTHORIZE)
                .queryParam(CODE_CHALLENGE_METHOD, CODE_CHALLENGE_METHOD_S256)
                .queryParam(RESPONSE_TYPE, RESPONSE_TYPE_CODE)
                .queryParam(CLIENT_ID, CLIENT_ID_VALUE)
                .queryParam(CODE_CHALLENGE, codeChallenge)
                .queryParam(REDRIECT_URI, REDIRECT_URI_VALUE)
                .queryParam(PFIDP_ADAPTER_ID, PFIDP_ADAPTER_ID_MOBILE)
                .queryParam(SCOPE, SCOPES_READ)
                .queryParam(STATE, randomState)
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
        final BankIdResponse response = new BankIdResponse();

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

    public BearerTokenResponse fetchBearerToken(String code) {
        final Form formBuilder = new Form();
        formBuilder.put(FormKeys.CODE, code);
        formBuilder.put(FormKeys.CODE_VERIFIER, sessionStorage.get(StorageKeys.CODE_VERIFIER));
        formBuilder.put(FormKeys.CLIENT_ID, FormValues.CLIENT_ID_SHORT);
        formBuilder.put(FormKeys.REDIRECT_URI, FormValues.REDIRECT_URI);
        formBuilder.put(FormKeys.GRANT_TYPE, FormValues.GRANT_TYPE_FOR_BEARER);
        formBuilder.put(FormKeys.CLIENT_SECRET, FormValues.CLIENT_SECRET_FOR_BEARER);

        return httpClient
                .request(Urls.FETCH_BEARER)
                .header(HeaderKeys.ADRUM_1, HeaderValues.ADRUM_1)
                .header(HeaderKeys.AUTHORIZATION, sessionStorage.get(StorageKeys.INIT_ACCESS_TOKEN))
                .header(HeaderKeys.ADRUM, HeaderValues.ADRUM)
                .header(HeaderKeys.SK_API_KEY, HeaderValues.SK_API_KEY)
                .body(formBuilder, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(BearerTokenResponse.class);
    }

    public FetchAccountResponse fetchAccounts() {
        return httpClient
                .request(Urls.FETCH_ACCOUNTS)
                .header(HeaderKeys.AUTHORIZATION, sessionStorage.get(StorageKeys.BEARER_TOKEN))
                .header(HeaderKeys.SK_API_KEY, HeaderValues.SK_API_KEY)
                .get(FetchAccountResponse.class);
    }

    public FetchAccountTransactionsResponse fetchAccountTransactions(
            String accountId, String page) {
        return httpClient
                .request(
                        Urls.FETCH_ACCOUNT_TRANSACTIONS
                                .parameter(IdTags.ACCOUNT_ID, accountId)
                                .parameter(IdTags.PAGE, page))
                .header(HeaderKeys.AUTHORIZATION, sessionStorage.get(StorageKeys.BEARER_TOKEN))
                .header(HeaderKeys.SK_API_KEY, HeaderValues.SK_API_KEY)
                .get(FetchAccountTransactionsResponse.class);
    }

    public FetchAccountTransactionsResponse fetchPendingAccountTransactions(String accountId) {
        return httpClient
                .request(
                        Urls.FETCH_PENDING_ACCOUNT_TRANSACTIONS.parameter(
                                IdTags.ACCOUNT_ID, accountId))
                .header(HeaderKeys.AUTHORIZATION, sessionStorage.get(StorageKeys.BEARER_TOKEN))
                .header(HeaderKeys.SK_API_KEY, HeaderValues.SK_API_KEY)
                .get(FetchAccountTransactionsResponse.class);
    }

    public FetchApprovedPaymentsResponse fetchApprovedPayments() {
        return httpClient
                .request(Urls.FETCH_APPROVED_PAYMENTS)
                .header(HeaderKeys.AUTHORIZATION, sessionStorage.get(StorageKeys.BEARER_TOKEN))
                .header(HeaderKeys.SK_API_KEY, HeaderValues.SK_API_KEY)
                .get(FetchApprovedPaymentsResponse.class);
    }

    public FetchCreditCardsResponse fetchCreditCards() {
        return httpClient
                .request(Urls.FETCH_CREDIT_CARDS)
                .header(HeaderKeys.AUTHORIZATION, sessionStorage.get(StorageKeys.BEARER_TOKEN))
                .header(HeaderKeys.SK_API_KEY, HeaderValues.SK_API_KEY)
                .get(FetchCreditCardsResponse.class);
    }

    public FetchInvestmentsResponse fetchInvestments() {
        return httpClient
                .request(Urls.FETCH_INVESTMENT_ACCOUNTS)
                .header(HeaderKeys.AUTHORIZATION, sessionStorage.get(StorageKeys.BEARER_TOKEN))
                .header(HeaderKeys.SK_API_KEY, HeaderValues.SK_API_KEY)
                .get(FetchInvestmentsResponse.class);
    }

    public FetchInvestmentAccountDetailsResponse fetchInvestmentAccountDetails(
            String investmentAccountNumber) {
        return httpClient
                .request(
                        Urls.FETCH_INVESTMENT_ACCOUNT_DETAILS.parameter(
                                IdTags.ACCOUNT_ID, investmentAccountNumber))
                .header(HeaderKeys.AUTHORIZATION, sessionStorage.get(StorageKeys.BEARER_TOKEN))
                .header(HeaderKeys.SK_API_KEY, HeaderValues.SK_API_KEY)
                .get(FetchInvestmentAccountDetailsResponse.class);
    }

    public FetchInvestmentHoldingsResponse fetchHoldings(String investmentAccountNumber) {
        return httpClient
                .request(
                        Urls.FETCH_INVESTMENT_HOLDINGS.parameter(
                                IdTags.ACCOUNT_ID, investmentAccountNumber))
                .header(HeaderKeys.AUTHORIZATION, sessionStorage.get(StorageKeys.BEARER_TOKEN))
                .header(HeaderKeys.SK_API_KEY, HeaderValues.SK_API_KEY)
                .get(FetchInvestmentHoldingsResponse.class);
    }

    public PensionFundsResponse fetchPensionHoldings(
            String partNumber, String encrypedNationalIdentificationNumber) {
        return httpClient
                .request(
                        Urls.FETCH_PENSIONS_HOLDINGS
                                .parameter(IdTags.PART_ID, partNumber)
                                .queryParam(
                                        ENCRYPED_NATIONAL_IDENTIFICATION_NUMBER,
                                        encrypedNationalIdentificationNumber))
                .header(HeaderKeys.AUTHORIZATION, sessionStorage.get(StorageKeys.BEARER_TOKEN))
                .header(HeaderKeys.SK_API_KEY, HeaderValues.SK_API_KEY)
                .get(PensionFundsResponse.class);
    }

    public IdentityDataResponse fetchIdentityData() {
        return httpClient
                .request(Urls.FETCH_IDENTITY)
                .header(HeaderKeys.AUTHORIZATION, sessionStorage.get(StorageKeys.BEARER_TOKEN))
                .header(HeaderKeys.SK_API_KEY, HeaderValues.SK_API_KEY)
                .get(IdentityDataResponse.class);
    }

    public void logout() {
        final Form formBuilder = new Form();
        formBuilder.put(FormKeys.CLIENT_SECRET, FormValues.CLIENT_SECRET_FOR_BEARER);
        formBuilder.put(FormKeys.REFRESH_TOKEN, sessionStorage.get(StorageKeys.REFRESH_TOKEN));
        formBuilder.put(FormKeys.CLIENT_ID, FormValues.CLIENT_ID_SHORT);

        httpClient
                .request(Urls.LOGOUT)
                .header(HeaderKeys.AUTHORIZATION, sessionStorage.get(StorageKeys.INIT_ACCESS_TOKEN))
                .header(HeaderKeys.SK_API_KEY, HeaderValues.SK_API_KEY)
                .body(formBuilder, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post();
    }
}
