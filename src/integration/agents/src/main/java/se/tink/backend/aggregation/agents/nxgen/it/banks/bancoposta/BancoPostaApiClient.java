package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta;

import java.util.Map;
import java.util.Random;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.RegisterCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.RegisterInitBody;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.RegisterInitCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.SendOtpRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.SimpleRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.Token;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.AccessTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.AuthorizationTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.CheckRegisterAppResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.InitRegistrationWithDigitalCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.RegisterInitResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.RegistrationWithDigitalCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.VerificationOnboardingResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc.AccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc.TransactionsResponse;
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
        return createBaseRequest(Urls.SEND_POSTE_CODE)
                .header(
                        HeaderValues.XKEY,
                        generateXKey(storage.getAppId(), storage.getOtpSecretKey()))
                .header(
                        HttpHeaders.AUTHORIZATION,
                        HeaderValues.BEARER + storage.getRegistrationSessionToken())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(RegistrationWithDigitalCodeResponse.class, requestBody);
    }

    public InitRegistrationWithDigitalCodeResponse initAccountWithDigitalCode(
            RegisterInitCodeRequest requestBody) {
        return createBaseRequest(Urls.INIT_CODE_VERIFICATION)
                .header(
                        HeaderValues.XKEY,
                        generateXKey(storage.getAppId(), storage.getOtpSecretKey()))
                .header(
                        HttpHeaders.AUTHORIZATION,
                        HeaderValues.BEARER + storage.getRegistrationSessionToken())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(InitRegistrationWithDigitalCodeResponse.class, requestBody);
    }

    public void sendSmsOTPWallet(SendOtpRequest baseRequest) {
        createBaseRequest(Urls.ELIMINA_WALLET)
                .header(
                        HeaderValues.XKEY,
                        generateXKey(storage.getAppId(), storage.getOtpSecretKey()))
                .header(
                        HttpHeaders.AUTHORIZATION,
                        HeaderValues.BEARER + storage.getRegistrationSessionToken())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(baseRequest);
    }

    public void requestForSmsOtpWallet(SimpleRequest baseRequest) {
        createBaseRequest(Urls.SEND_OTP)
                .header(
                        HeaderValues.XKEY,
                        generateXKey(storage.getAppId(), storage.getOtpSecretKey()))
                .header(
                        HttpHeaders.AUTHORIZATION,
                        HeaderValues.BEARER + storage.getRegistrationSessionToken())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(baseRequest);
    }

    public void initSyncWallet(SimpleRequest baseRequest) {
        createBaseRequest(Urls.INIT_SYNC_WALLET)
                .header(
                        HeaderValues.XKEY,
                        generateXKey(storage.getAppId(), storage.getOtpSecretKey()))
                .header(
                        HttpHeaders.AUTHORIZATION,
                        HeaderValues.BEARER + storage.getRegistrationSessionToken())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(baseRequest);
    }

    public Token performSecondOpenIdAz(String form) {
        HttpResponse response =
                createBaseRequest(Urls.AUTH_OPENID_AZ)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(HttpResponse.class, form);

        if ("login_required".equals(response.getBody(Map.class).get("error"))) {
            throw LoginError.DEFAULT_MESSAGE.exception(
                    "Something went wrong with JWE structure in request call");
        }

        return response.getBody(Token.class);
    }

    public AuthorizationTransactionResponse authorizeTransaction(String jwe) {
        return createBaseRequest(Urls.AUTHORIZE_TRANSACTION)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(AuthorizationTransactionResponse.class, jwe);
    }

    public String challenge(String jwe) {
        return createBaseRequest(Urls.CHALLENGE)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                .post(String.class, jwe);
    }

    public CheckRegisterAppResponse checkRegisterApp(String jwe) {
        return createBaseRequest(Urls.CHECK_REGISTER).post(CheckRegisterAppResponse.class, jwe);
    }

    public Map<String, String> registerApp(String jwe) {
        return createBaseRequest(Urls.REGISTER_APP).post(Map.class, jwe);
    }

    public void activate(String activationBody) {
        createBaseRequest(Urls.ACTIVATION)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .header("X-ENC-DEVID", "")
                .post(RegisterInitResponse.class, activationBody);
    }

    public String register(String obj) {
        return createBaseRequest(Urls.REGISTER).post(String.class, obj);
    }

    public RegisterInitResponse registerInit(RegisterInitBody registerInitBody) {
        return createBaseRequest(Urls.REGISTER_INIT)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(RegisterInitResponse.class, registerInitBody);
    }

    public String performRequestAz(String azBody) {
        return createBaseRequest(Urls.AUTH_REQ_AZ)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .post(String.class, azBody);
    }

    public AccessTokenResponse performOpenIdAz(Form form) {
        HttpResponse response =
                createBaseRequest(Urls.AUTH_OPENID_AZ)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(HttpResponse.class, form.serialize());

        if ("login_required".equals(response.getBody(Map.class).get("error"))) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        return response.getBody(AccessTokenResponse.class);
    }

    public String performJwtAuthorization() {
        return createBaseRequest(Urls.AUTH_JWT)
                .header("X-RJWT", "sso:https://www.meniga.com")
                .post(String.class);
    }

    public VerificationOnboardingResponse verifyOnboarding(SimpleRequest body) {
        return createBaseRequest(Urls.ONBOARDING_VERIFICATION)
                .header(
                        HeaderValues.XKEY,
                        generateXKey(storage.getAppId(), storage.getOtpSecretKey()))
                .header(
                        HttpHeaders.AUTHORIZATION,
                        HeaderValues.BEARER + storage.getAccessBasicToken())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(VerificationOnboardingResponse.class, body);
    }

    private RequestBuilder createBaseRequest(URL url) {
        return httpClient.request(url).accept(HeaderValues.ACCEPT);
    }

    private String generateXKey(String appUid, byte[] otpSecretKey) {
        byte[] otpSecretKeyByte = otpSecretKey;
        long movingFactor = new Random().nextInt();
        String otp = HOTP.generateOTP(otpSecretKeyByte, movingFactor, 8, 20);
        return String.format("%s:%s:%s", appUid, otp, movingFactor);
    }

    public AccountsResponse fetchAccounts() {
        return createBaseRequest(Urls.FETCH_ACCOUNTS)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        HeaderValues.BEARER + storage.getAccessDataToken())
                .get(AccountsResponse.class);
    }

    public AccountDetailsResponse fetchAccountDetails(AccountDetailsRequest accountDetailsRequest) {
        return createBaseRequest(Urls.FETCH_ACCOUNT_DETAILS)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(
                        HeaderValues.XKEY,
                        generateXKey(storage.getAppId(), storage.getOtpSecretKey()))
                .header(
                        HttpHeaders.AUTHORIZATION,
                        HeaderValues.BEARER + storage.getAccessBasicToken())
                .post(AccountDetailsResponse.class, accountDetailsRequest);
    }

    public TransactionsResponse fetchTransactions(TransactionsRequest transactionsRequest) {
        return createBaseRequest(Urls.FETCH_TRANSACTIONS)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(
                        HeaderValues.XKEY,
                        generateXKey(storage.getAppId(), storage.getOtpSecretKey()))
                .header(
                        HttpHeaders.AUTHORIZATION,
                        HeaderValues.BEARER + storage.getAccessBasicToken())
                .post(TransactionsResponse.class, transactionsRequest);
    }
}
