package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta;

import java.util.Map;
import java.util.Random;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.UserContext;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.RegisterInitBody;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.RequestBody;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.AccessTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.AuthorizationTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.CheckRegisterAppResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.InitRegistrationWithDigitalCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.RegisterInitResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.RegistrationWithDigitalCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.VerificationOnboardingResponse;
import se.tink.backend.aggregation.agents.utils.crypto.HOTP;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BancoPostaApiClient {

    private final TinkHttpClient httpClient;
    private final UserContext userContext;

    BancoPostaApiClient(TinkHttpClient httpClient, UserContext userContext) {
        this.httpClient = httpClient;
        this.userContext = userContext;
    }

    public RegistrationWithDigitalCodeResponse registerWithDigitalCode(RequestBody requestBody) {
        return createBaseRequest(Urls.SEND_POSTE_CODE)
                .header(
                        HeaderValues.XKEY,
                        generateXKey(userContext.getAppId(), userContext.getOtpSecretKey()))
                .header(
                        HttpHeaders.AUTHORIZATION,
                        HeaderValues.BEARER + userContext.getRegistrationSessionToken())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(RegistrationWithDigitalCodeResponse.class, requestBody);
    }

    public InitRegistrationWithDigitalCodeResponse initAccountWithDigitalCode(
            RequestBody requestBody) {
        return createBaseRequest(Urls.INIT_CODE_VERIFICATION)
                .header(
                        HeaderValues.XKEY,
                        generateXKey(userContext.getAppId(), userContext.getOtpSecretKey()))
                .header(
                        HttpHeaders.AUTHORIZATION,
                        HeaderValues.BEARER + userContext.getRegistrationSessionToken())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(InitRegistrationWithDigitalCodeResponse.class, requestBody);
    }

    public void sendSmsOTPWallet(RequestBody requestBody) {
        createBaseRequest(Urls.ELIMINA_WALLET)
                .header(
                        HeaderValues.XKEY,
                        generateXKey(userContext.getAppId(), userContext.getOtpSecretKey()))
                .header(
                        HttpHeaders.AUTHORIZATION,
                        HeaderValues.BEARER + userContext.getRegistrationSessionToken())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(requestBody);
    }

    public void requestForSmsOtpWallet(RequestBody requestBody) {
        createBaseRequest(Urls.SEND_OTP)
                .header(
                        HeaderValues.XKEY,
                        generateXKey(userContext.getAppId(), userContext.getOtpSecretKey()))
                .header(
                        HttpHeaders.AUTHORIZATION,
                        HeaderValues.BEARER + userContext.getRegistrationSessionToken())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(requestBody);
    }

    public void initSyncWallet(RequestBody requestBody) {
        createBaseRequest(Urls.INIT_SYNC_WALLET)
                .header(
                        HeaderValues.XKEY,
                        generateXKey(userContext.getAppId(), userContext.getOtpSecretKey()))
                .header(
                        HttpHeaders.AUTHORIZATION,
                        HeaderValues.BEARER + userContext.getRegistrationSessionToken())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(requestBody);
    }

    public String performSecondOpenIdAz(String form) {
        HttpResponse response =
                createBaseRequest(Urls.AUTH_OPENID_AZ)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(HttpResponse.class, form);

        if ("login_required".equals(response.getBody(Map.class).get("error"))) {
            throw LoginError.DEFAULT_MESSAGE.exception(
                    "Something went wrong with probably JWE structure in request call");
        }

        return response.getBody(String.class);
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

    public VerificationOnboardingResponse verifyOnboarding(RequestBody body) {
        return createBaseRequest(Urls.ONBOARDING_VERIFICATION)
                .header(
                        HeaderValues.XKEY,
                        generateXKey(userContext.getAppId(), userContext.getOtpSecretKey()))
                .header(
                        HttpHeaders.AUTHORIZATION,
                        HeaderValues.BEARER + userContext.getRegistrationSessionToken())
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
}
