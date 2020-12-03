package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis;

import agents_platform_agents_framework.org.springframework.http.HttpMethod;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.RequestEntity.BodyBuilder;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants.MeanIds;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.AuthResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.DataSetEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.DeviceInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.EBankingUserId;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.EasyPinEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.OathEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.SignDataEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.TransactionDataEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.UcrEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.CheckLoginResultRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.CheckLoginResultResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.EasyPinActivateRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.EasyPinActivateResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.EasyPinCreateRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.EasyPinCreateResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.EasyPinProvisionResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.ExecuteSignRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.ExecuteSignResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.InitializeLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.InitializeLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.InitiateSignRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.InitiateSignResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy.AuthenticationProcessRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy.AuthenticationProcessResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy.CheckForcedUpgradeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy.DistributorAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy.EBankingUsersRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy.EbankingUsersResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy.GenerateChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy.UserInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper.FortisRandomTokenGenerator;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AgentPlatformFortisApiClient {

    private static final String EMPTY_BODY = "";

    private final AgentPlatformHttpClient client;
    private final FortisRandomTokenGenerator fortisRandomTokenGenerator;
    private final String baseUrl;
    private final String distributorId;
    private final String csrf;

    public AgentPlatformFortisApiClient(
            AgentPlatformHttpClient client,
            FortisRandomTokenGenerator fortisRandomTokenGenerator,
            String baseUrl,
            String distributorId) {
        this.client = client;
        this.fortisRandomTokenGenerator = fortisRandomTokenGenerator;
        this.baseUrl = baseUrl;
        this.distributorId = distributorId;
        this.csrf = fortisRandomTokenGenerator.generateCSRF();
    }

    public InitializeLoginResponse initializeLoginTransaction(String cardNumber, String smid) {
        InitializeLoginRequest initializeLoginRequest =
                InitializeLoginRequest.builder()
                        .authenticationFactorId(cardNumber)
                        .distributorId(distributorId)
                        .language(FortisConstants.LANGUAGE)
                        .smid(smid)
                        .minimumDacLevel(FortisConstants.MINIMUM_DAC_LEVEL)
                        .requestedMeanId(FortisConstants.MeanIds.UCR)
                        .build();

        BodyBuilder bodyBuilder =
                getBodyBuilder(HttpMethod.POST, getUrl(Urls.INITIALIZE_LOGIN_TRANSACTION));
        addDefaultCookies(bodyBuilder);
        addDefaultUserAgent(bodyBuilder);

        ResponseEntity<String> responseEntity =
                client.exchange(bodyBuilder.body(initializeLoginRequest), String.class, null);

        return parseInitializeLoginResponse(responseEntity);
    }

    public CheckLoginResultResponse checkLoginResult(String smid, String signature) {
        CheckLoginResultRequest checkLoginResultRequest =
                CheckLoginResultRequest.builder()
                        .distributorId(distributorId)
                        .smid(smid)
                        .ucr(new UcrEntity(signature))
                        .build();

        BodyBuilder bodyBuilder = getBodyBuilder(HttpMethod.POST, getUrl(Urls.CHECK_LOGIN_RESULT));
        addDefaultCookies(bodyBuilder);
        addDefaultUserAgent(bodyBuilder);

        return send(bodyBuilder, CheckLoginResultResponse.class, checkLoginResultRequest);
    }

    public void doEbewAppLogin(String smid, String meanId) {
        Form form =
                Form.builder()
                        .put(
                                "auth",
                                "{\"authType\":\"UCRS\",\"mean_id\":\"UCRS\",\"dist_id\":\""
                                        + distributorId
                                        + "\",\"ebanking_user_id\":{\"smid\":\""
                                        + smid
                                        + "\"},\"ebanking_user_authenticity_validation\":{\"authentication_mean_id\":\""
                                        + meanId
                                        + "\"}}")
                        .build();

        BodyBuilder bodyBuilder =
                getBodyBuilder(
                        HttpMethod.POST,
                        getUrl(Urls.EBEW_APP_LOGIN_V2),
                        MediaType.APPLICATION_FORM_URLENCODED);
        addDefaultUserAgent(bodyBuilder);
        addDefaultCookies(bodyBuilder);

        send(bodyBuilder, Void.class, form.serialize());
    }

    public UserInfoResponse getUserInfo() {
        BodyBuilder bodyBuilder = getBodyBuilder(HttpMethod.POST, getUrl(Urls.GET_USER_INFO));
        addDefaultUserAgent(bodyBuilder);
        addDefaultCookies(bodyBuilder);

        return send(bodyBuilder, UserInfoResponse.class, EMPTY_BODY);
    }

    public void getCountryList() {
        BodyBuilder bodyBuilder = getBodyBuilder(HttpMethod.POST, getUrl(Urls.GET_USER_INFO));
        addDefaultUserAgent(bodyBuilder);
        addDefaultCookies(bodyBuilder);

        send(bodyBuilder, Void.class, EMPTY_BODY);
    }

    public EasyPinCreateResponse easyPinCreate(String mobileNumber, String appId) {
        DeviceInfoEntity deviceInfoEntity =
                DeviceInfoEntity.builder()
                        .deviceModel(DeviceProfileConfiguration.IOS_STABLE.getModelNumber())
                        .appVersion(FortisConstants.APP_VERSION)
                        .deviceBrand(DeviceProfileConfiguration.IOS_STABLE.getMake())
                        .deviceName(FortisConstants.DEVICE_NAME)
                        .appIdentity(appId)
                        .deviceIdentity(appId)
                        .freeText("")
                        .build();

        EasyPinCreateRequest easyPinCreateRequest =
                EasyPinCreateRequest.builder()
                        .deviceInfo(deviceInfoEntity)
                        .mobileNumber(mobileNumber)
                        .build();

        BodyBuilder bodyBuilder = getBodyBuilder(HttpMethod.POST, getUrl(Urls.EASYPIN_CREATE));
        addDefaultUserAgent(bodyBuilder);
        addDefaultCookies(bodyBuilder);

        return send(bodyBuilder, EasyPinCreateResponse.class, easyPinCreateRequest);
    }

    public EasyPinProvisionResponse easyPinProvision(
            String encQueryData, String registrationCode, String enrollmentSessionId, String smid) {
        Form form =
                Form.builder()
                        .put(FortisConstants.Form.METHOD, FortisConstants.EASY_PIN_METHOD)
                        .put(
                                FortisConstants.Form.PROTOCOL_VERSION,
                                FortisConstants.EASY_PIN_PROTOCOL_VERSION)
                        .put(
                                FortisConstants.Form.PUBLIC_KEY_ID,
                                FortisConstants.EASY_PIN_PUBLIC_KEY_ID)
                        .put(FortisConstants.Form.ENC_QUERY_DATA, encQueryData)
                        .build();

        BodyBuilder bodyBuilder =
                getBodyBuilder(
                        HttpMethod.POST,
                        getUrl(Urls.EASYPIN_PROVISION),
                        MediaType.APPLICATION_FORM_URLENCODED);
        bodyBuilder.header(
                FortisConstants.Headers.USER_AGENT,
                "FortisApp/2.0 CFNetwork/978.0.7 Darwin/18.7.0");
        bodyBuilder.header(
                FortisConstants.Headers.COOKIE,
                getManualCookies()
                        + ";registrationCode="
                        + registrationCode
                        + ";smid="
                        + smid
                        + ";ENROLLMENT-SESSION-ID="
                        + enrollmentSessionId);

        EasyPinProvisionResponse response =
                send(bodyBuilder, EasyPinProvisionResponse.class, form.serialize());

        if ("FAILED".equals(response.getMessage())) {
            // Means a problem with our provisioning (encryption and query data)
            throw new IllegalStateException("Server did not allow provisioning");
        }

        return response;
    }

    public EasyPinActivateResponse easyPinActivate(
            String otp, String smid, String gsn, String tokenId) {
        EasyPinActivateRequest easyPinActivateRequest =
                EasyPinActivateRequest.builder()
                        .otp(otp)
                        .smid(smid)
                        .gsn(gsn)
                        .distributorId(distributorId)
                        .oath(new OathEntity(tokenId))
                        .build();

        BodyBuilder bodyBuilder = getBodyBuilder(HttpMethod.POST, getUrl(Urls.EASYPIN_ACTIVATE));
        addDefaultUserAgent(bodyBuilder);
        addDefaultCookies(bodyBuilder);

        return send(bodyBuilder, EasyPinActivateResponse.class, easyPinActivateRequest);
    }

    public InitiateSignResponse initiateSignTransaction(String tokenId) {
        InitiateSignRequest initiateSignRequest =
                InitiateSignRequest.builder()
                        .transactionDatas(
                                Collections.singletonList(
                                        TransactionDataEntity.builder()
                                                .signatureType(FortisConstants.SIGNATURE_TYPE)
                                                .tokenId(tokenId)
                                                .securityType(FortisConstants.SECURITY_TYPE)
                                                .build()))
                        .applicationCode(FortisConstants.APPLICATION_CODE)
                        .orderType(FortisConstants.TYPE_EASYPIN_BIOMETRIC)
                        .signData(
                                new SignDataEntity(
                                        new DataSetEntity("true", "biometricActivation")))
                        .build();

        BodyBuilder bodyBuilder =
                getBodyBuilder(HttpMethod.POST, getUrl(Urls.INITIATE_SIGN_TRANSACTION));
        addDefaultUserAgent(bodyBuilder);
        addDefaultCookies(bodyBuilder);

        return send(bodyBuilder, InitiateSignResponse.class, initiateSignRequest);
    }

    public void retrieveSignMeans() {
        BodyBuilder bodyBuilder =
                getBodyBuilder(HttpMethod.GET, getUrl(Urls.RETRIEVE_AUTHORISED_SIGN_MEANS));
        addDefaultUserAgent(bodyBuilder);
        addDefaultCookies(bodyBuilder);

        sendWithoutBody(bodyBuilder, Void.class);
    }

    public ExecuteSignResponse executeSignTransaction() {
        ExecuteSignRequest executeSignRequest =
                new ExecuteSignRequest(FortisConstants.MeanIds.EAPI);

        BodyBuilder bodyBuilder =
                getBodyBuilder(HttpMethod.POST, getUrl(Urls.EXECUTE_SIGN_TRANSACTION));
        addDefaultUserAgent(bodyBuilder);
        addDefaultCookies(bodyBuilder);

        return send(bodyBuilder, ExecuteSignResponse.class, executeSignRequest);
    }

    /** Auto auth variant of the call */
    public InitializeLoginResponse initializeLoginTransaction(
            String maskedCardNumber,
            String cardFrameId,
            String smid,
            String oathTokenId,
            String deviceId) {
        InitializeLoginRequest initializeLoginRequest =
                InitializeLoginRequest.builder()
                        .authenticationFactorId(maskedCardNumber)
                        .distributorId(distributorId)
                        .language(FortisConstants.LANGUAGE)
                        .smid(smid)
                        .minimumDacLevel(FortisConstants.MINIMUM_DAC_LEVEL)
                        .requestedMeanId(FortisConstants.MeanIds.EAPI)
                        .cardFrameId(cardFrameId)
                        .deviceInfo(
                                DeviceInfoEntity.builder()
                                        .fingerPrint(deviceId)
                                        .appIdentity(deviceId)
                                        .name(FortisConstants.DEVICE_NAME)
                                        .tokenId(oathTokenId)
                                        .build())
                        .build();

        BodyBuilder bodyBuilder =
                getBodyBuilder(HttpMethod.POST, getUrl(Urls.INITIALIZE_LOGIN_TRANSACTION));
        addDefaultUserAgent(bodyBuilder);
        addDefaultCookies(bodyBuilder);

        ResponseEntity<String> responseEntity =
                client.exchange(bodyBuilder.body(initializeLoginRequest), String.class);

        return parseInitializeLoginResponse(responseEntity);
    }

    public CheckLoginResultResponse checkLoginResultEasyPin(String smid, String easyPinResponse) {
        CheckLoginResultRequest checkLoginResultRequest =
                CheckLoginResultRequest.builder()
                        .distributorId(distributorId)
                        .smid(smid)
                        .easyPin(new EasyPinEntity(easyPinResponse))
                        .build();

        BodyBuilder bodyBuilder = getBodyBuilder(HttpMethod.POST, getUrl(Urls.CHECK_LOGIN_RESULT));
        addDefaultUserAgent(bodyBuilder);
        addDefaultCookies(bodyBuilder);

        return send(bodyBuilder, CheckLoginResultResponse.class, checkLoginResultRequest);
    }

    /** Legacy auth call */
    private void checkForcedUpgrade() {
        CheckForcedUpgradeRequest request = new CheckForcedUpgradeRequest(distributorId);

        BodyBuilder bodyBuilder =
                getBodyBuilder(HttpMethod.POST, getUrl(Urls.Legacy.CHECK_FORCED_UPGRADE));
        addDefaultUserAgent(bodyBuilder);
        addDefaultCookies(bodyBuilder);

        send(bodyBuilder, Void.class, SerializationUtils.serializeToString(request));
    }

    /** Legacy auth call */
    public void getDistributorAuthenticationMeans() {
        DistributorAuthenticationRequest request =
                new DistributorAuthenticationRequest(
                        "",
                        FortisConstants.LEGACY_DISTRIBUTION_CHANNEL_ID,
                        FortisConstants.MINIMUM_DAC_LEVEL,
                        distributorId);

        BodyBuilder bodyBuilder =
                getBodyBuilder(
                        HttpMethod.POST, getUrl(Urls.Legacy.GET_DISTRIBUTOR_AUTHENTICATION_MEANS));
        addDefaultUserAgent(bodyBuilder);
        addDefaultCookies(bodyBuilder);

        send(bodyBuilder, Void.class, SerializationUtils.serializeToString(request));
    }

    /** Legacy auth call */
    public EbankingUsersResponse getEBankingUsers(String authenticationFactorId, String smid) {
        // These two calls MUST be made in this order. Otherwise correct cookies will not be set!
        checkForcedUpgrade();
        getDistributorAuthenticationMeans();

        EBankingUsersRequest eBankingUsersRequest =
                new EBankingUsersRequest(
                        authenticationFactorId,
                        distributorId,
                        smid,
                        FortisConstants.LEGACY_STATIC_CARDFRAME_ID);

        BodyBuilder bodyBuilder =
                getBodyBuilder(HttpMethod.POST, getUrl(Urls.Legacy.GET_E_BANKING_USERS));
        addDefaultUserAgent(bodyBuilder);
        addDefaultCookies(bodyBuilder);

        return send(
                bodyBuilder,
                EbankingUsersResponse.class,
                SerializationUtils.serializeToString(eBankingUsersRequest));
    }

    /** Legacy auth call */
    public AuthenticationProcessResponse createAuthenticationProcess(
            EBankingUserId eBankingUserId) {
        AuthenticationProcessRequest authenticationProcessRequest =
                new AuthenticationProcessRequest(eBankingUserId, distributorId, MeanIds.LEGACY);

        BodyBuilder bodyBuilder =
                getBodyBuilder(HttpMethod.POST, getUrl(Urls.Legacy.CREATE_AUTHENTICATION_PROCESS));
        addDefaultUserAgent(bodyBuilder);
        addDefaultCookies(bodyBuilder);

        return send(
                bodyBuilder,
                AuthenticationProcessResponse.class,
                SerializationUtils.serializeToString(authenticationProcessRequest));
    }

    /** Legacy auth call */
    public ChallengeResponse fetchChallenges(String authenticationProcessId) {

        GenerateChallengeRequest challengeRequest =
                new GenerateChallengeRequest(distributorId, authenticationProcessId);

        BodyBuilder bodyBuilder =
                getBodyBuilder(HttpMethod.POST, getUrl(Urls.Legacy.GENERATE_CHALLENGES));
        addDefaultUserAgent(bodyBuilder);
        addDefaultCookies(bodyBuilder);

        return send(
                bodyBuilder,
                ChallengeResponse.class,
                SerializationUtils.serializeToString(challengeRequest));
    }

    /** Legacy auth call */
    public String authenticationRequest(
            String authenticationProcessId,
            String agreementId,
            String maskedCardNumber,
            String smid,
            String challenge,
            String calculateChallenge,
            String deviceFingerprint) {

        AuthResponse authResponse =
                AuthResponse.builder()
                        .withAuthProcId(authenticationProcessId)
                        .withAgreementId(agreementId)
                        .withAuthenticationMeanId(MeanIds.LEGACY)
                        .withCardNumber(maskedCardNumber)
                        .withDistId(distributorId)
                        .withSmid(smid)
                        .withChallenge(challenge)
                        .withResponse(calculateChallenge)
                        .withDeviceFingerprint(deviceFingerprint)
                        .withMeanId("DIDAP")
                        .build();

        BodyBuilder bodyBuilder =
                getBodyBuilder(
                        HttpMethod.POST,
                        getUrl(Urls.Legacy.AUTHENTICATION_URL),
                        MediaType.APPLICATION_FORM_URLENCODED);
        addDefaultUserAgent(bodyBuilder);
        addDefaultCookies(bodyBuilder);

        return send(bodyBuilder, String.class, authResponse.getUrlEncodedFormat());
    }

    private BodyBuilder getBodyBuilder(HttpMethod httpMethod, URI uri, String contentType) {
        return RequestEntity.method(httpMethod, uri)
                .header(FortisConstants.Headers.CSRF, csrf)
                .header(FortisConstants.Headers.CONTENT_TYPE, contentType);
    }

    private BodyBuilder getBodyBuilder(HttpMethod httpMethod, URI uri) {
        return RequestEntity.method(httpMethod, uri)
                .header(FortisConstants.Headers.CSRF, csrf)
                .header(FortisConstants.Headers.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }

    private void addDefaultCookies(BodyBuilder bodyBuilder) {
        bodyBuilder.header(FortisConstants.Headers.COOKIE, getManualCookies());
    }

    private void addDefaultUserAgent(BodyBuilder bodyBuilder) {
        bodyBuilder.header(FortisConstants.Headers.USER_AGENT, getUserAgent());
    }

    @SuppressWarnings("unchecked")
    private <T> T send(BodyBuilder bodyBuilder, Class<T> responseClass, Object request) {
        String responseBody =
                client.exchange(bodyBuilder.body(request), String.class, null).getBody();
        if (responseClass == String.class) {
            return (T) responseBody;
        }
        return SerializationUtils.deserializeFromString(responseBody, responseClass);
    }

    private <T> T sendWithoutBody(BodyBuilder bodyBuilder, Class<T> responseClass) {
        String responseBody = client.exchange(bodyBuilder.build(), String.class, null).getBody();
        return SerializationUtils.deserializeFromString(responseBody, responseClass);
    }

    private String getManualCookies() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(FortisConstants.Cookie.CSRF).append("=").append(csrf);
        stringBuilder.append(";");
        stringBuilder
                .append(FortisConstants.Cookie.AXES)
                .append("=")
                .append(fortisRandomTokenGenerator.generateAxes());
        stringBuilder.append(";");
        stringBuilder
                .append(FortisConstants.Cookie.DEVICE_FEATURES)
                .append("=")
                .append(FortisConstants.HeaderValues.DEVICE_FEATURES_VALUE);
        stringBuilder.append(";");
        stringBuilder
                .append(FortisConstants.Cookie.DISTRIBUTOR_ID)
                .append("=")
                .append(distributorId);
        stringBuilder.append(";");
        stringBuilder
                .append(FortisConstants.Cookie.EUROPOLICY)
                .append("=")
                .append(FortisConstants.Cookie.EUROPOLICY_OPTIN);

        return stringBuilder.toString();
    }

    private InitializeLoginResponse parseInitializeLoginResponse(
            ResponseEntity<String> responseEntity) {

        List<String> cookies = responseEntity.getHeaders().get("Set-Cookie");

        String loginSessionId = null;
        if (cookies != null) {
            loginSessionId =
                    cookies.stream()
                            .filter(cookie -> cookie.contains("loginSessionId"))
                            .map(
                                    cookie -> {
                                        int idx = cookie.indexOf("loginSessionId");
                                        int endIdx = cookie.indexOf(';', idx);
                                        if (endIdx == -1) {
                                            endIdx = cookie.length();
                                        }
                                        return cookie.substring(
                                                idx + "loginSessionId=".length(), endIdx);
                                    })
                            .findAny()
                            .orElse("Unknown");
        }

        InitializeLoginResponse response =
                SerializationUtils.deserializeFromString(
                        responseEntity.getBody(), InitializeLoginResponse.class);
        response.setLoginSessionId(loginSessionId);
        return response;
    }

    private URI getUrl(String resource) {
        return URI.create(String.format("%s%s", baseUrl, resource));
    }

    private String getUserAgent() {
        String mozillaVersion =
                DeviceProfileConfiguration.IOS_STABLE.getUserAgentEntity().getMozillaVersion();

        String iphoneModel = DeviceProfileConfiguration.IOS_STABLE.getModelNumber();
        String iOSVersion = DeviceProfileConfiguration.IOS_STABLE.getOsVersion();
        String appleWebKit =
                DeviceProfileConfiguration.IOS_STABLE.getUserAgentEntity().getPlatform();
        String platformDetails =
                DeviceProfileConfiguration.IOS_STABLE.getUserAgentEntity().getPlatformDetails();
        String extra =
                String.format(
                        "Mobile/7D11 FAT/ APPTYPE=001/ APPVERSION=%s/OS=ios-phone",
                        FortisConstants.APP_VERSION);

        return String.format(
                "%s (%s; U;iOS %s; en-us) %s %s %s",
                mozillaVersion, iphoneModel, iOSVersion, appleWebKit, platformDetails, extra);
    }
}
