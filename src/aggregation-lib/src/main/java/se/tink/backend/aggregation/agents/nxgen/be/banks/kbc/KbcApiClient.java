package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc;

import com.google.common.base.Charsets;
import com.google.common.primitives.Bytes;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import org.assertj.core.util.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.ActivationInstanceRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.ActivationInstanceResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.ActivationLicenseRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.ActivationLicenseResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.ActivationVerificationRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.ActivationVerificationResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.ChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.ChallengeSotpRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.ChallengeSotpResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.EnrollDeviceRoundOneRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.EnrollDeviceRoundOneResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.EnrollDeviceRoundTwoRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.EnrollDeviceRoundTwoResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.KeyExchangeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.KeyExchangeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.LoginSotpRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.LoginSotpResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.LogoutResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.PersonalisationResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.RegisterLogonRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.RegisterLogonResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.HeaderDto;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.SignChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.SignChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.SignRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.SignTypesResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.SignValidationRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.SignValidationResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto.SigningChallengeSotpResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto.SigningChallengeUcrResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto.TransferRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto.TransferResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto.ValidateTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.BeneficiariesResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.FutureTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.FutureTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.TransactionsHistoryRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.TransactionsHistoryResponse;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class KbcApiClient {
    private final SessionStorage sessionStorage;
    private final TinkHttpClient client;

    private KbcApiClient(SessionStorage sessionStorage, TinkHttpClient client) {
        this.sessionStorage = sessionStorage;
        this.client = client;
    }

    public static KbcApiClient create(SessionStorage sessionStorage, TinkHttpClient client) {
        return new KbcApiClient(sessionStorage, client);
    }

    // == START PRIVATE METHODS ==
    // On some endpoint KBC response start with ")]}'," on the first line and the json object on the second.
    private <T> T cleanResponse(HttpResponse httpResponse, Class<T> responseClass) {
        String responseBody = httpResponse.getBody(String.class);
        if (responseBody == null) {
            return null;
        }

        String cleanString = responseBody.replaceFirst("\\)]}',\n", "");

        return SerializationUtils.deserializeFromString(cleanString, responseClass);
    }

    private String getResultCodeOrThrow(HeaderDto header) {
        return Optional.ofNullable(header)
                .map(HeaderDto::getResultCode)
                .map(TypeValuePair::getValue).orElseThrow(
                () -> new IllegalStateException("Did not get any result code in response."));
    }

    private void verifyDoubleZeroResponseCode(HeaderDto header) {
        verifyResponseCode(header, KbcConstants.ResultCode.DOUBLE_ZERO);
    }

    private void verifyResponseCode(HeaderDto header, final String expectedValue) {
        String resultValue = getResultCodeOrThrow(header);

        if (!Objects.equals(expectedValue, resultValue)) {
            throwInvalidResultCodeError(header, resultValue);
        }
    }

    private void throwInvalidResultCodeError(HeaderDto header, String resultCode) {
        String resultMessage = Optional.ofNullable(header)
                .map(HeaderDto::getResultMessage)
                .map(TypeValuePair::getValue).orElse("");

        throw new IllegalStateException(
                String.format("Invalid result code - [ResultCode]: %s - [Message]: %s",
                        resultCode, resultMessage));
    }

    private void generateAndStoreCipherKey() {
        final byte[] key = RandomUtils.secureRandom(KbcConstants.Encryption.AES_KEY_LENGTH);
        sessionStorage.put(KbcConstants.Encryption.AES_SESSION_KEY_KEY, EncodingUtils.encodeAsBase64String(key));
    }

    private byte[] getCipherKey() {
        return EncodingUtils.decodeBase64String(sessionStorage.get(KbcConstants.Encryption.AES_SESSION_KEY_KEY));
    }

    // All responses are prepended with the iv used when encrypting the body.
    // This method will extract the iv from the response.
    private byte[] getCipherIv(byte[] cipherBytes) {
        return Arrays.copyOfRange(cipherBytes, 0, KbcConstants.Encryption.IV_LENGTH);
    }

    // Extract the body of the response containing the ciphertext
    private byte[] getCipherBody(byte[] cipherBytes) {
        return Arrays.copyOfRange(cipherBytes, KbcConstants.Encryption.IV_LENGTH, cipherBytes.length);
    }

    private String encryptAndEncodePublicKey() {
        PublicKey publicKey = RSA.getPubKeyFromBytes(
                EncodingUtils.decodeBase64String(KbcConstants.Encryption.PUBLIC_KEY));
        byte[] cipherText = RSA.encryptNonePkcs1((RSAPublicKey) publicKey, getCipherKey());
        return EncodingUtils.encodeAsBase64String(cipherText);
    }

    private <T> String encryptAndEncodeRequest(T request) {
        String serializedRequest = SerializationUtils.serializeToString(request);
        byte[] iv = RandomUtils.secureRandom(16);
        byte[] encryptedRequest = AES.encryptCbc(getCipherKey(), iv, serializedRequest.getBytes());
        byte[] concatenatedArrays = Bytes.concat(iv, encryptedRequest);
        return EncodingUtils.encodeAsBase64String(concatenatedArrays);
    }

    private <T> T decodeAndDecryptResponse(HttpResponse httpResponse, Class<T> responseClass) {
        String responseBody = httpResponse.getBody(String.class);
        byte[] cipherBytes = EncodingUtils.decodeBase64String(responseBody);
        byte[] decryptedResponse = AES.decryptCbc(getCipherKey(), getCipherIv(cipherBytes), getCipherBody(cipherBytes));
        String response = new String(decryptedResponse, Charsets.UTF_8);
        return deserializeFromString(response, responseClass);
    }

    private <T> T deserializeFromString(String response, Class<T> responseClass) {
        if (String.class.equals(responseClass)) {
            return (T) response;
        } else {
            return SerializationUtils.deserializeFromString(response, responseClass);
        }
    }

    private <T> T post(
            KbcConstants.Url url, Object request, Class<T> responseType) {
        return post(url, request, responseType, true);
    }

    private <T> T post(
            KbcConstants.Url url, Object request, Class<T> responseType, boolean encryptAndEncodeRequest) {

        String completeRequest = request != null && encryptAndEncodeRequest
                ? encryptAndEncodeRequest(request) : SerializationUtils.serializeToString(request);

        HttpResponse httpResponse = request != null ?
                client.request(url.get()).post(HttpResponse.class, completeRequest) :
                client.request(url.get()).post(HttpResponse.class);

        T response = encryptAndEncodeRequest ?
                decodeAndDecryptResponse(httpResponse, responseType) : cleanResponse(httpResponse, responseType);

        return response;
    }

    // == END PRIVATE METHODS ==

    public void prepareSession() {
        generateAndStoreCipherKey();
        keyExchange(KbcConstants.RequestInput.COMPANY_ID, KbcConstants.RequestInput.APP_FAMILY);
    }

    public void logout() {
        LogoutResponse response = post(KbcConstants.Url.LOGOUT, null, LogoutResponse.class);
        verifyDoubleZeroResponseCode(response.getHeader());
    }

    public KeyExchangeResponse keyExchange(String companyId, String appFamily) {
        KeyExchangeRequest request = KeyExchangeRequest.createWithStandardTypes(
                companyId, appFamily, encryptAndEncodePublicKey());

        KeyExchangeResponse response = post(KbcConstants.Url.KEY_EXCHANGE,
                request, KeyExchangeResponse.class, false);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }

    public String challenge() {
        ChallengeRequest challengeRequest = ChallengeRequest.createWithStandardTypes(
                KbcConstants.RequestInput.AUTHENTICATION_TYPE, KbcConstants.LANGUAGE);

        ChallengeResponse response = post(KbcConstants.Url.CHALLENGE, challengeRequest, ChallengeResponse.class);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response.getChallenge().getValue();
    }

    public RegisterLogonResponse registerLogon(String username, String challengeResponse) {
        RegisterLogonRequest registerLogonRequest = RegisterLogonRequest.builder()
                .applicationId(KbcConstants.ApplicationId.REGISTER_LOGON)
                .captcha(KbcConstants.RequestInput.EMPTY_CAPTCHA)
                .company(KbcConstants.RequestInput.COMPANY_ID)
                .language(KbcConstants.LANGUAGE)
                .response(challengeResponse)
                .saveCardNumber(KbcConstants.RequestInput.SAVE_CARD_NUMBER)
                .ucrType(KbcConstants.RequestInput.UCR_TYPE)
                .username(username).build();

        RegisterLogonResponse response =
                post(KbcConstants.Url.REGISTER_LOGON, registerLogonRequest, RegisterLogonResponse.class);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }

    public PersonalisationResponse personalisation() {
        PersonalisationResponse response = post(KbcConstants.Url.PERSONALISATION,
                null, PersonalisationResponse.class);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }

    public String enrollDevice() {
        EnrollDeviceRoundOneRequest enrollDeviceRequest = EnrollDeviceRoundOneRequest.createWithStandardTypes(
                KbcConstants.RequestInput.IS_POLITICAL_PROMINENT_PERSION_CHECKED,
                KbcConstants.ApplicationId.APPLICATION_TYPE_CODE,
                KbcConstants.RequestInput.IS_COMMON_REPOST_STANDARD_CHECKED,
                KbcConstants.RequestInput.APP_CONDITIONS_CHECKED,
                KbcConstants.RequestInput.BANK_CONDITIONS_CHECKED,
                KbcConstants.RequestInput.DOCCLE_CHECKED);

        EnrollDeviceRoundOneResponse response = post(KbcConstants.Url.ENROLL_DEVICE,
                enrollDeviceRequest, EnrollDeviceRoundOneResponse.class);
        verifyResponseCode(response.getHeader(), KbcConstants.ResultCode.ZERO_NINE);

        return response.getHeader().getSigningId().getEncoded();
    }

    public String signTypeManual(String signingId) {
        SignRequest signRequest = SignRequest.createWithSigningId(signingId);

        SignTypesResponse signTypesResponse =
                post(KbcConstants.Url.SIGNING_TYPES, signRequest, SignTypesResponse.class);
        verifyDoubleZeroResponseCode(signTypesResponse.getHeader());

        return signTypesResponse.getSignTypeId(KbcConstants.Predicates.SIGN_TYPE_MANUAL);
    }

    public String signChallenge(String signTypeId, String signingId) {
        SignChallengeRequest signChallengeRequest = SignChallengeRequest.create(signTypeId, signingId);

        SignChallengeResponse signChallengeResponse =
                post(KbcConstants.Url.SIGNING_CHALLENGE, signChallengeRequest, SignChallengeResponse.class);
        verifyDoubleZeroResponseCode(signChallengeResponse.getHeader());

        return signChallengeResponse.getChallenge().getValue();
    }

    public String signValidation(String signingResponse, String panNr, String signingId) {
        SignValidationRequest signValidationRequest = SignValidationRequest.create(signingResponse, panNr, signingId);

        SignValidationResponse signValidationResponse =
                post(KbcConstants.Url.SIGNING_VALIDATION, signValidationRequest, SignValidationResponse.class);
        verifyDoubleZeroResponseCode(signValidationResponse.getHeader());

        return signValidationResponse.getHeader().getSigningId().getEncoded();
    }

    public EnrollDeviceRoundTwoResponse enrollDeviceWithSigningId(String signingId) {
        EnrollDeviceRoundTwoRequest enrollDeviceRoundTwoRequest = EnrollDeviceRoundTwoRequest.create(signingId);

        EnrollDeviceRoundTwoResponse enrollDeviceRoundTwoResponse = post(KbcConstants.Url.ENROLL_DEVICE,
                enrollDeviceRoundTwoRequest,
                EnrollDeviceRoundTwoResponse.class);
        verifyDoubleZeroResponseCode(enrollDeviceRoundTwoResponse.getHeader());

        return enrollDeviceRoundTwoResponse;
    }

    public ActivationLicenseResponse activationLicence(KbcDevice device, String iv,
            String encryptedClientPublicKeyAndNonce) {
        ActivationLicenseRequest activationLicenseRequest = ActivationLicenseRequest.builder()
                .applicationId(KbcConstants.RequestInput.APPLICATION_ID)
                .applicationTypeCode(KbcConstants.ApplicationId.APPLICATION_TYPE_CODE)
                .applicationVersionNo(KbcConstants.RequestInput.VERSION_NUMBER)
                .clientInitialVector(iv)
                .companyNo(KbcConstants.RequestInput.COMPANY_ID)
                .deviceId(device.getDeviceId())
                .encryptedClientPublicKeyAndNonce(encryptedClientPublicKeyAndNonce)
                .language(KbcConstants.LANGUAGE)
                .logonId(device.getAccessNumber()).build();

        ActivationLicenseResponse activationLicenseResponse = post(
                KbcConstants.Url.ACTIVATION_LICENSE, activationLicenseRequest, ActivationLicenseResponse.class);
        verifyDoubleZeroResponseCode(activationLicenseResponse.getHeader());

        return activationLicenseResponse;
    }

    public String activationInstance(KbcDevice device, String iv, String encryptedNonce, String challenge,
            String deviceCode) {
        ActivationInstanceRequest activationInstanceRequest = ActivationInstanceRequest.builder()
                .applicationId(KbcConstants.RequestInput.APPLICATION_ID)
                .applicationTypeCode(KbcConstants.ApplicationId.APPLICATION_TYPE_CODE)
                .applicationVersionNo(KbcConstants.RequestInput.VERSION_NUMBER)
                .challenge(challenge)
                .clientInitialVector(iv)
                .companyNo(KbcConstants.RequestInput.COMPANY_ID)
                .deviceCode(deviceCode)
                .deviceId(device.getDeviceId())
                .encryptedServerNonce(encryptedNonce)
                .logonId(device.getAccessNumber())
                .build();

        ActivationInstanceResponse activationInstanceResponse = post(
                KbcConstants.Url.ACTIVATION_INSTANCE, activationInstanceRequest, ActivationInstanceResponse.class);
        verifyDoubleZeroResponseCode(activationInstanceResponse.getHeader());

        return activationInstanceResponse.getActivationMessage().getValue();
    }

    public ActivationVerificationResponse activationVerification(KbcDevice device, String verificationMessage) {
        ActivationVerificationRequest activationVerificationRequest = ActivationVerificationRequest.builder()
                .applicationId(KbcConstants.RequestInput.APPLICATION_ID)
                .applicationTypeCode(KbcConstants.ApplicationId.APPLICATION_TYPE_CODE)
                .applicationVersionNo(KbcConstants.RequestInput.VERSION_NUMBER)
                .companyNo(KbcConstants.RequestInput.COMPANY_ID)
                .deviceId(device.getDeviceId())
                .logonId(device.getAccessNumber())
                .verificationMessage(verificationMessage)
                .activationMessage(device.getActivationMessage())
                .osVersionNo(KbcConstants.RequestInput.OS_VERSION_NO)
                .osType(KbcConstants.RequestInput.OS_TYPE)
                .fingerprint(device.getFingerprint())
                .build();

        ActivationVerificationResponse activationVerificationResponse = post(
                KbcConstants.Url.ACTIVATION_VERIFICATION, activationVerificationRequest,
                ActivationVerificationResponse.class);
        verifyDoubleZeroResponseCode(activationVerificationResponse.getHeader());

        return activationVerificationResponse;
    }

    public String challengeSotp(KbcDevice device) {
        ChallengeSotpRequest challengeRequest = ChallengeSotpRequest.builder()
                .setApplicationId(KbcConstants.RequestInput.APPLICATION_ID)
                .setApplicationTypeCode(KbcConstants.ApplicationId.APPLICATION_TYPE_CODE)
                .setApplicationVersionNo(KbcConstants.RequestInput.VERSION_NUMBER)
                .setCompanyNo(KbcConstants.RequestInput.COMPANY_ID)
                .setDeviceId(device.getDeviceId())
                .setDeviceName(KbcConstants.RequestInput.DEVICE_NAME)
                .setLogonId(device.getAccessNumber())
                .setOsVersionNo(KbcConstants.RequestInput.OS_VERSION_NO)
                .setOsType(KbcConstants.RequestInput.OS_TYPE)
                .setFingerprint(device.getFingerprint())
                .build();

        ChallengeSotpResponse challengeResponse = post(
                KbcConstants.Url.CHALLENGE_SOTP, challengeRequest, ChallengeSotpResponse.class);
        verifyDoubleZeroResponseCode(challengeResponse.getHeader());

        return challengeResponse.getChallenge().getValue();
    }

    public LoginSotpResponse loginSotp(KbcDevice device, String otp) {
        LoginSotpRequest loginSotpRequest = LoginSotpRequest.builder()
                .setApplicationId(KbcConstants.RequestInput.APPLICATION_ID)
                .setAppType(KbcConstants.ApplicationId.APPLICATION_TYPE_CODE)
                .setAppVersion(KbcConstants.RequestInput.VERSION_NUMBER)
                .setCompany(KbcConstants.RequestInput.COMPANY_ID)
                .setDeviceId(device.getDeviceId())
                .setDeviceType(KbcConstants.RequestInput.DEVICE_NAME)
                .setOtp(otp)
                .setUsername(device.getAccessNumber())
                .setWithTouchId(KbcConstants.RequestInput.WITH_TOUCH_ID)
                .setOsVersion(KbcConstants.RequestInput.OS_VERSION_NO)
                .setOs(KbcConstants.RequestInput.OS_TYPE)
                .setFingerprint(device.getFingerprint())
                .build();

        LoginSotpResponse response = post(KbcConstants.Url.LOGIN_SOTP, loginSotpRequest, LoginSotpResponse.class);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }

    public AccountsResponse fetchAccounts() {
        AccountsRequest accountsRequest = AccountsRequest.builder()
                .setBalanceIndicator(true)
                .setIncludeReservationsIndicator(true)
                .setIncludeAgreementMakeUp(true)
                .setRetrieveSavingsAccountsOnlyIndicator(false)
                .setRetrieveCurrentAccountsOnlyIndicator(false)
                .setPaymentDashboardIndicator(true)
                .build();

        AccountsResponse response = post(KbcConstants.Url.ACCOUNTS, accountsRequest, AccountsResponse.class);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }

    public TransactionsHistoryResponse fetchTransactions(String accountNo, String repositioningKey) {
        Preconditions.checkNotNull(accountNo);

        TransactionsHistoryRequest request = TransactionsHistoryRequest.builder()
                .setAccountNo(accountNo)
                .setRepositioningKey(repositioningKey != null ? repositioningKey : "")
                .setCompanyNo(KbcConstants.RequestInput.COMPANY_ID)
                .setCurrency(KbcConstants.RequestInput.CURRENCY)
                .setRoleCode(KbcConstants.RequestInput.ROLE_CODE)
                .setSearchAmount(KbcConstants.RequestInput.SEARCH_AMOUNT)
                .setSearchMessage(KbcConstants.RequestInput.SEARCH_MESSAGE)
                .setTransactionsQuantity(KbcConstants.RequestInput.TRANSACTIONS_QUANTITY)
                .build();

        TransactionsHistoryResponse response =
                post(KbcConstants.Url.TRANSACTIONS_HISTORY, request, TransactionsHistoryResponse.class);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }

    public FutureTransactionsResponse fetchFutureTransactions(String accountNo, String repositioningKey) {
        Preconditions.checkNotNull(accountNo);

        FutureTransactionsRequest request = FutureTransactionsRequest.builder()
                .setAccountNo(accountNo)
                .setRepositioningKey(repositioningKey != null ? repositioningKey : "")
                .setTransactionsQuantity(KbcConstants.RequestInput.TRANSACTIONS_QUANTITY)
                .setCurrencyCode(KbcConstants.RequestInput.CURRENCY)
                .build();

        FutureTransactionsResponse response =
                post(KbcConstants.Url.FUTURE_TRANSACTIONS, request, FutureTransactionsResponse.class);

        String resultCode = getResultCodeOrThrow(response.getHeader());
        if (!KbcConstants.ResultCode.DOUBLE_ZERO.equals(resultCode)
                && !KbcConstants.ResultCode.ZERO_TWO.equals(resultCode)) {
            throwInvalidResultCodeError(response.getHeader(), resultCode);
        }

        return response;
    }

    public String fetchCards() {
        return post(KbcConstants.Url.CARDS, null, String.class);
    }

    public AccountsResponse accountsForTransferToOwn() {
        AccountsRequest request = AccountsRequest.builder()
                .setIncludeAgreementMakeUp(true)
                .setIncludeReservationsIndicator(true)
                .build();

        AccountsResponse response =
                post(KbcConstants.Url.ACCOUNTS_FOR_TRANSFER_TO_OWN, request, AccountsResponse.class);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }

    public AccountsResponse accountsForTransferToOther() {
        AccountsRequest request = AccountsRequest.builder()
                .setIncludeAgreementMakeUp(true)
                .setIncludeReservationsIndicator(true)
                .build();

        AccountsResponse response =
                post(KbcConstants.Url.ACCOUNTS_FOR_TRANSFER_TO_OTHER, request, AccountsResponse.class);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }

    public BeneficiariesResponse beneficiariesHistory() {
        BeneficiariesResponse response =
                post(KbcConstants.Url.BENEFICIARIES_HISTORY, null, BeneficiariesResponse.class);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }

    public ValidateTransferResponse validateTransfer(Transfer transfer, boolean isTransferToOwnAccount) {
        TransferRequest request = TransferRequest.create(transfer, isTransferToOwnAccount);

        ValidateTransferResponse response =
                post(KbcConstants.Url.TRANSFER_VALIDATE, request, ValidateTransferResponse.class);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }

    public String prepareTransfer(Transfer transfer, boolean isTransferToOwnAccount) {
        KbcConstants.Url url = isTransferToOwnAccount
                ? KbcConstants.Url.TRANSFER_TO_OWN
                : KbcConstants.Url.TRANSFER_TO_OTHER;
        TransferRequest request = TransferRequest.create(transfer, isTransferToOwnAccount);

        SignValidationResponse response = post(url, request, SignValidationResponse.class);
        verifyResponseCode(response.getHeader(), KbcConstants.ResultCode.ZERO_NINE);

        return response.getHeader().getSigningId().getEncoded();
    }

    public SignTypesResponse signingTypes(String signingId) {
        SignRequest signRequest = SignRequest.createWithSigningId(signingId);

        SignTypesResponse signTypesResponse =
                post(KbcConstants.Url.MOB_A031_SIGNING_TYPES, signRequest, SignTypesResponse.class);
        verifyDoubleZeroResponseCode(signTypesResponse.getHeader());

        return signTypesResponse;
    }

    public SigningChallengeSotpResponse signingChallengeSotp(String signTypeId, String signingId) {
        SignChallengeRequest request = SignChallengeRequest.create(signTypeId, signingId);

        SigningChallengeSotpResponse response =
                post(KbcConstants.Url.MOB_A031_SIGNING_CHALLENGE_SOTP, request, SigningChallengeSotpResponse.class);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }

    public void signingValidationSotp(String signingResponse, String panNr, String signingId) {
        SignValidationRequest signValidationRequest = SignValidationRequest.create(signingResponse, panNr, signingId);

        SignValidationResponse signValidationResponse =
                post(KbcConstants.Url.MOB_A031_SIGNING_VALIDATION_SOTP, signValidationRequest, SignValidationResponse.class);
        verifyDoubleZeroResponseCode(signValidationResponse.getHeader());
    }

    public SigningChallengeUcrResponse signingChallengeUcr(String signTypeId, String signingId) {
        SignChallengeRequest request = SignChallengeRequest.create(signTypeId, signingId);

        SigningChallengeUcrResponse response =
                post(KbcConstants.Url.MOB_A031_SIGNING_CHALLENGE_UCR, request, SigningChallengeUcrResponse.class);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }

    public void signingValidationUcr(String signingResponse, String panNr, String signingId) {
        SignValidationRequest signValidationRequest = SignValidationRequest.create(signingResponse, panNr, signingId);

        SignValidationResponse signValidationResponse =
                post(KbcConstants.Url.MOB_A031_SIGNING_VALIDATION_UCR, signValidationRequest, SignValidationResponse.class);
        verifyDoubleZeroResponseCode(signValidationResponse.getHeader());
    }

    public TransferResponse signTransfer(String signingId, boolean isTransferToOwnAccount) {
        KbcConstants.Url url = isTransferToOwnAccount
                ? KbcConstants.Url.TRANSFER_TO_OWN
                : KbcConstants.Url.TRANSFER_TO_OTHER;

        SignRequest request = SignRequest.createWithSigningId(signingId);

        TransferResponse response = post(url, request, TransferResponse.class);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }
}
