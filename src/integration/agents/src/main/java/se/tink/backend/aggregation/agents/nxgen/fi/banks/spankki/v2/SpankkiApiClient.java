package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2;

import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.Authentication;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc.ChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc.EncapResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc.KeyCardLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc.KeyCardLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc.PhoneResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc.ReceiveOtpRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc.SolveChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc.UserPasswordLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc.VerifyOtpRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc.VerifyOtpResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard.rpc.CreditCardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.investment.rpc.InstrumentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.investment.rpc.InvestmentAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.loan.rpc.LoansResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.transactionalaccount.rpc.TransactionalAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.rpc.SpankkiHeader;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.rpc.SpankkiResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.rpc.RequestBody;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SpankkiApiClient {
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final TinkHttpClient client;

    public SpankkiApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        // Encap does not like Tink headers ;(
        client.disableSignatureRequestHeader();
        client.setUserAgent(Headers.SPANKKI_USER_AGENT);
    }

    public SpankkiResponse keepAlive() {
        if (persistentStorage.get(Storage.DEVICE_ID) == null) {
            throw new IllegalStateException("Cannot keep-alive with empty storage");
        }
        return getRequest(SpankkiResponse.class, Urls.KEEP_ALIVE);
    }

    public ChallengeResponse receiveChallenge() {
        final String randomString = UUID.randomUUID().toString();
        final String requestToken = calculateRequestToken(randomString);
        final ChallengeRequest challengeRequest = new ChallengeRequest(requestToken, randomString);

        return postRequest(ChallengeResponse.class, Urls.REQUEST_CHALLENGE, challengeRequest);
    }

    public ChallengeResponse solveChallenge(SolveChallengeRequest solveChallengeRequest) {
        return postRequest(ChallengeResponse.class, Urls.RESPONSE_CHALLENGE, solveChallengeRequest);
    }

    public UserPasswordLoginResponse userPasswordLogin(String username, String password) {
        final LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        return postRequest(UserPasswordLoginResponse.class, Urls.LOGIN_USERPWD, loginRequest);
    }

    public KeyCardLoginResponse keyCardLogin(String code) {
        final KeyCardLoginRequest keyCardLoginRequest = new KeyCardLoginRequest();
        final String hardwareId = UUID.randomUUID().toString();
        // This same hardware id will have to be used in encap.
        sessionStorage.put(Storage.HARDWARE_ID, hardwareId);
        keyCardLoginRequest.setHardwareId(hardwareId);
        keyCardLoginRequest.setPin(code);

        return postRequest(KeyCardLoginResponse.class, Urls.LOGIN_KEYCARD, keyCardLoginRequest);
    }

    public EncapResponse startEncap() {
        return postRequest(EncapResponse.class, Urls.START_ENCAP);
    }

    public EncapResponse pollEncap() {
        return postRequest(EncapResponse.class, Urls.POLL_ENCAP);
    }

    public String encap(Map<String, String> encapParams) {
        return client.request(Urls.ENCAP)
                .accept(MediaType.WILDCARD)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(String.class, new RequestBody(encapParams));
    }

    public PhoneResponse getPhonenumber() {
        return getRequest(PhoneResponse.class, Urls.GET_PHONENUMBER);
    }

    public SpankkiResponse receiveOtp(String phonenumber) {
        final ReceiveOtpRequest receiveOtpRequest = new ReceiveOtpRequest(phonenumber);

        return postRequest(SpankkiResponse.class, Urls.RECEIVE_OTP, receiveOtpRequest);
    }

    public VerifyOtpResponse verifyOtpResponse(String otp) {
        final VerifyOtpRequest verifyOtpRequest = new VerifyOtpRequest(otp);

        return postRequest(VerifyOtpResponse.class, Urls.VERIFY_OTP, verifyOtpRequest);
    }

    public TransactionalAccountsResponse fetchAccounts() {
        return getRequest(TransactionalAccountsResponse.class, Urls.FETCH_ACCOUNTS);
    }

    public TransactionsResponse fetchTransactions(String accountId, String page) {
        return getRequest(
                TransactionsResponse.class,
                Urls.FETCH_TRANSACTIONS
                        .parameter(IdTags.ACCOUNT_ID, accountId)
                        .parameter(IdTags.PAGE, page));
    }

    public CreditCardsResponse fetchCards() {
        return getRequest(CreditCardsResponse.class, Urls.FETCH_CARDS);
    }

    public CreditCardDetailsResponse fetchCardDetails(String contractNr, String productCode) {
        return getRequest(
                CreditCardDetailsResponse.class,
                Urls.FETCH_CARD_DETAILS
                        .queryParam(QueryKeys.CONTRACT_NR, contractNr)
                        .queryParam(QueryKeys.PRODUCT_CODE, productCode));
    }

    public CreditCardTransactionsResponse fetchCardTransactions(
            String contractNr, String fromDate, String toDate) {
        return getRequest(
                CreditCardTransactionsResponse.class,
                Urls.FETCH_CARD_TRANSACTIONS
                        .parameter(IdTags.CONTRACT_NR, contractNr)
                        .parameter(IdTags.FROM_DATE, fromDate)
                        .parameter(IdTags.TO_DATE, toDate));
    }

    public InvestmentAccountResponse fetchInvestmentsAccount() {
        return getRequest(InvestmentAccountResponse.class, Urls.FETCH_INVESTMENT_ACCOUNT);
    }

    public InstrumentDetailsResponse fetchInstrumentDetails(String portfolioId, String securityId) {
        return getRequest(
                InstrumentDetailsResponse.class,
                Urls.FETCH_FUND_DETAILS
                        .queryParam(QueryKeys.PORTFOLIO_ID, portfolioId)
                        .queryParam(QueryKeys.SECURITY_ID, securityId));
    }

    public LoansResponse fetchLoans() {
        return getRequest(LoansResponse.class, Urls.FETCH_LOANS);
    }

    public LoanDetailsResponse fetchLoanDetails() {
        return getRequest(LoanDetailsResponse.class, Urls.FETCH_LOAN_DETAILS);
    }

    private String calculateRequestToken(String randomString) {
        final String randomBytesString = randomString + Authentication.REQUEST_TOKEN_HASH_SALT;

        return EncodingUtils.encodeAsBase64String(Hash.sha256(randomBytesString));
    }

    private <T extends SpankkiResponse> T getRequest(Class<T> c, URL url) {
        final String spankkiHeader = createSpankkiHeader();

        final T response =
                client.request(url)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                        .header(Headers.X_SMOB_KEY, spankkiHeader)
                        .get(c);

        return validateResponse(response);
    }

    private <T extends SpankkiResponse> T postRequest(Class<T> c, URL url) {
        final String spankkiHeader = createSpankkiHeader();

        final T response =
                client.request(url)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                        .header(Headers.X_SMOB_KEY, spankkiHeader)
                        .post(c);

        return validateResponse(response);
    }

    private <T extends SpankkiResponse, U> T postRequest(Class<T> c, URL url, U body) {
        final String spankkiHeader = createSpankkiHeader();

        final T response =
                client.request(url)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(Headers.X_SMOB_KEY, spankkiHeader)
                        .post(c, body);

        return validateResponse(response);
    }

    private String createSpankkiHeader() {
        final SpankkiHeader spankkiHeader = new SpankkiHeader();
        spankkiHeader.setSessionId(sessionStorage.get(Storage.SESSION_ID));
        spankkiHeader.setDeviceId(persistentStorage.get(Storage.DEVICE_ID));
        return SerializationUtils.serializeToString(spankkiHeader);
    }

    private <T extends SpankkiResponse> T validateResponse(T response) {
        if (response.getStatus().isBankSideFailure()) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Status code: "
                            + response.getStatus().getStatusCode()
                            + ", message: "
                            + response.getStatus().getMessage());
        }
        sessionStorage.put(Storage.SESSION_ID, response.getStatus().getSessionId());

        return response;
    }
}
