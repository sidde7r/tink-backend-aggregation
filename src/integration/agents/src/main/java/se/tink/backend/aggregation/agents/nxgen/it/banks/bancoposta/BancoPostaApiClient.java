package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta;

import java.security.SecureRandom;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Urls.AuthUrl;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Urls.CheckingAccUrl;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Urls.IdentityUrl;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Urls.InvestmentUrl;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Urls.SavingAccUrl;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.RegisterCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.RegisterInitBody;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.RegisterInitCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.SendOtpRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.Token;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.AccessTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.AuthorizationTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.CheckRegisterAppResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.InitRegistrationWithDigitalCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.RegisterInitResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.RegistrationWithDigitalCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.VerificationOnboardingResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.common.rpc.SimpleRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.checking.rpc.AccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.checking.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.checking.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.checking.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.checking.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.investment.rpc.InvestmentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.saving.rpc.SavingAccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.saving.rpc.SavingAccountDetailsTransactionRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.saving.rpc.SavingAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.saving.rpc.SavingTransactionResponse;
import se.tink.backend.aggregation.agents.utils.crypto.HOTP;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class BancoPostaApiClient {

    private final TinkHttpClient httpClient;
    private final BancoPostaStorage storage;

    public RegistrationWithDigitalCodeResponse registerWithDigitalCode(
            RegisterCodeRequest requestBody) {
        return createBaseRequestWithBearerTokenAndXKey(
                        storage.getRegistrationSessionToken(), AuthUrl.SEND_POSTE_CODE)
                .post(RegistrationWithDigitalCodeResponse.class, requestBody);
    }

    public InitRegistrationWithDigitalCodeResponse initAccountWithDigitalCode(
            RegisterInitCodeRequest requestBody) {
        return createBaseRequestWithBearerTokenAndXKey(
                        storage.getRegistrationSessionToken(), AuthUrl.INIT_CODE_VERIFICATION)
                .post(InitRegistrationWithDigitalCodeResponse.class, requestBody);
    }

    public void sendSmsOTPWallet(SendOtpRequest baseRequest) {
        createBaseRequestWithBearerTokenAndXKey(
                        storage.getRegistrationSessionToken(), AuthUrl.ELIMINA_WALLET)
                .post(baseRequest);
    }

    public void requestForSmsOtpWallet(SimpleRequest baseRequest) {
        createBaseRequestWithBearerTokenAndXKey(
                        storage.getRegistrationSessionToken(), AuthUrl.SEND_OTP)
                .post(baseRequest);
    }

    public void initSyncWallet(SimpleRequest baseRequest) {
        createBaseRequestWithBearerTokenAndXKey(
                        storage.getRegistrationSessionToken(), AuthUrl.INIT_SYNC_WALLET)
                .post(baseRequest);
    }

    public Token performSecondOpenIdAz(String form) {
        HttpResponse response =
                createBaseRequest(AuthUrl.AUTH_OPENID_AZ)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(HttpResponse.class, form);

        if ("login_required".equals(response.getBody(Map.class).get("error"))) {
            throw LoginError.DEFAULT_MESSAGE.exception(
                    "Something went wrong with JWE structure in request call");
        }

        return response.getBody(Token.class);
    }

    public AuthorizationTransactionResponse authorizeTransaction(String jwe) {
        return createBaseRequest(AuthUrl.AUTHORIZE_TRANSACTION)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(AuthorizationTransactionResponse.class, jwe);
    }

    public String challenge(String jwe) {
        return createBaseRequest(AuthUrl.CHALLENGE)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                .post(String.class, jwe);
    }

    public CheckRegisterAppResponse checkRegisterApp(String jwe) {
        return createBaseRequest(AuthUrl.CHECK_REGISTER).post(CheckRegisterAppResponse.class, jwe);
    }

    public Map<String, String> registerApp(String jwe) {
        return createBaseRequest(AuthUrl.REGISTER_APP).post(Map.class, jwe);
    }

    public void activate(String activationBody) {
        createBaseRequest(AuthUrl.ACTIVATION)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .header("X-ENC-DEVID", "")
                .post(RegisterInitResponse.class, activationBody);
    }

    public String register(String obj) {
        return createBaseRequest(AuthUrl.REGISTER).post(String.class, obj);
    }

    public RegisterInitResponse registerInit(RegisterInitBody registerInitBody) {
        return createBaseRequest(AuthUrl.REGISTER_INIT)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(RegisterInitResponse.class, registerInitBody);
    }

    public String performRequestAz(String azBody) {
        return createBaseRequest(AuthUrl.AUTH_REQ_AZ)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .post(String.class, azBody);
    }

    public AccessTokenResponse performOpenIdAz(Form form) {
        HttpResponse response =
                createBaseRequest(AuthUrl.AUTH_OPENID_AZ)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(HttpResponse.class, form.serialize());

        if ("login_required".equals(response.getBody(Map.class).get("error"))) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        return response.getBody(AccessTokenResponse.class);
    }

    public String performJwtAuthorization() {
        return createBaseRequest(AuthUrl.AUTH_JWT)
                .header("X-RJWT", "sso:https://www.meniga.com")
                .post(String.class);
    }

    public VerificationOnboardingResponse verifyOnboarding(SimpleRequest body) {
        return createBaseRequestWithBearerTokenAndXKey(
                        storage.getRegistrationSessionToken(), AuthUrl.ONBOARDING_VERIFICATION)
                .post(VerificationOnboardingResponse.class, body);
    }

    private RequestBuilder createBaseRequest(URL url) {
        return httpClient.request(url).accept(HeaderValues.ACCEPT);
    }

    private RequestBuilder createBaseRequestWithBearerToken(String token, URL url) {
        return createBaseRequest(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, HeaderValues.BEARER + token);
    }

    private RequestBuilder createBaseRequestWithBearerTokenAndXKey(String token, URL url) {
        return createBaseRequestWithBearerToken(token, url)
                .header(
                        HeaderValues.XKEY,
                        generateXKey(storage.getAppId(), storage.getOtpSecretKey()));
    }

    private String generateXKey(String appUid, byte[] otpSecretKey) {
        long movingFactor = new SecureRandom().nextInt();
        String otp = HOTP.generateOTP(otpSecretKey, movingFactor, 8, 20);
        return String.format("%s:%s:%s", appUid, otp, movingFactor);
    }

    public AccountsResponse fetchAccounts() {
        return createBaseRequestWithBearerTokenAndXKey(
                        storage.getAccessBasicToken(), CheckingAccUrl.FETCH_ACCOUNTS)
                .post(AccountsResponse.class, new SimpleRequest());
    }

    public AccountDetailsResponse fetchAccountDetails(AccountDetailsRequest accountDetailsRequest) {
        return createBaseRequestWithBearerTokenAndXKey(
                        storage.getAccessBasicToken(), CheckingAccUrl.FETCH_ACCOUNT_DETAILS)
                .post(AccountDetailsResponse.class, accountDetailsRequest);
    }

    public TransactionsResponse fetchTransactions(TransactionsRequest transactionsRequest) {
        return createBaseRequestWithBearerTokenAndXKey(
                        storage.getAccessBasicToken(), CheckingAccUrl.FETCH_TRANSACTIONS)
                .post(TransactionsResponse.class, transactionsRequest);
    }

    public SavingAccountResponse fetchSavingAccounts(SimpleRequest requestBody) {

        return createBaseRequestWithBearerTokenAndXKey(
                        storage.getAccessBasicToken(), SavingAccUrl.FETCH_SAVING_ACCOUNTS)
                .post(SavingAccountResponse.class, requestBody);
    }

    public SavingAccountDetailsResponse fetchSavingAccountDetails(
            SavingAccountDetailsTransactionRequest request) {
        return createBaseRequestWithBearerTokenAndXKey(
                        storage.getAccessBasicToken(), SavingAccUrl.FETCH_SAVING_ACCOUNTS_DETAILS)
                .post(SavingAccountDetailsResponse.class, request);
    }

    public SavingTransactionResponse fetchSavingTransactions(
            SavingAccountDetailsTransactionRequest request) {
        return createBaseRequestWithBearerTokenAndXKey(
                        storage.getAccessBasicToken(), SavingAccUrl.FETCH_SAVING_TRANSACTIONS)
                .post(SavingTransactionResponse.class, request);
    }

    public String fetchIdentityData() {
        return createBaseRequestWithBearerToken(
                        storage.getAccessBasicToken(), IdentityUrl.FETCH_IDENTITY_DATA)
                .post(String.class);
    }

    public InvestmentResponse fetchInvestments() {
        return createBaseRequestWithBearerTokenAndXKey(
                        storage.getAccessBasicToken(), InvestmentUrl.FETCH_INVESTMENTS)
                .post(InvestmentResponse.class, new SimpleRequest());
    }
}
