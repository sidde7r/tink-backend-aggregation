package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc;

import static se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants.DEFAULT_LANGUAGE_FOR_PARSE_ERROR_TEXTS;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.primitives.Bytes;
import java.lang.invoke.MethodHandles;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import org.assertj.core.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants.Url;
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
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.RegisterLogonRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto.RegisterLogonResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.HeaderDto;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.SignChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.SignChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.SignRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.SignTypesResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.SignValidationRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.SignValidationResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeEncValueTuple;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AgreementDto;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AssetJarsDto;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AssetsDetailResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AssetsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.FutureTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.FutureTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.InvestmentPlanDetailResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.InvestmentPlanDto;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.InvestmentPlansOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.TransactionsHistoryRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.TransactionsHistoryResponse;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

public class KbcApiClient {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TinkHttpClient client;
    private AccountsResponse accountResponse;

    KbcApiClient(TinkHttpClient client) {
        this.client = client;
    }

    // == START PRIVATE METHODS ==
    // On some endpoint KBC response start with ")]}'," on the first line and the json object on the
    // second.
    private <T> T cleanResponse(HttpResponse httpResponse, Class<T> responseClass) {
        String responseBody = httpResponse.getBody(String.class);
        if (responseBody == null) {
            return null;
        }

        String cleanString = responseBody.replaceFirst("\\)]}',\n", "");

        return SerializationUtils.deserializeFromString(cleanString, responseClass);
    }

    private void checkBlockedAccount(HeaderDto headerDto, String errorHeader)
            throws AuthorizationException {
        String resultValue = getResultCodeOrThrow(headerDto);
        boolean matchesErrorMessages =
                matchesErrorMessage(
                                headerDto.getResultMessage(),
                                KbcConstants.ErrorMessage.ACCOUNT_BLOCKED)
                        || matchesErrorMessage(
                                headerDto.getResultMessage(),
                                KbcConstants.ErrorMessage.ACCOUNT_BLOCKED2);
        if (Objects.equals(KbcConstants.ResultCode.ZERO_TWO, resultValue) && matchesErrorMessages) {
            logger.warn(
                    "tag={} Header: {} Error message: {}",
                    KbcConstants.LogTags.ERROR_CODE_MESSAGE,
                    errorHeader,
                    headerDto.getResultMessage());
            throw AuthorizationError.ACCOUNT_BLOCKED.exception();
        }
    }

    private String getResultCodeOrThrow(HeaderDto header) {
        return Optional.ofNullable(header)
                .map(HeaderDto::getResultCode)
                .map(TypeValuePair::getValue)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Did not get any result code in response."));
    }

    private void verifyDoubleZeroResponseCode(HeaderDto header) {
        verifyResponseCode(header, KbcConstants.ResultCode.DOUBLE_ZERO, "");
    }

    private void verifyResponseCodeForLogin(HeaderDto header) throws LoginException {
        String resultValue = getResultCodeOrThrow(header);
        if (!Objects.equals(KbcConstants.ResultCode.DOUBLE_ZERO, resultValue)) {
            bankServerErrors(header, resultValue);
            notEnoughFundsCancelTransfer(header, resultValue);
            switch (resultValue) {
                case KbcConstants.ErrorCodes.AUTHENTICATION_ERROR:
                    throw LoginError.INCORRECT_CREDENTIALS.exception();

                case KbcConstants.ErrorCodes.INVALID_SIGN_CODE_LAST_TRY:
                    throw LoginError.INCORRECT_CREDENTIALS_LAST_ATTEMPT.exception();

                default:
                    throwInvalidResultCodeError(header, resultValue, "");
            }
        }
    }

    private void verifyResponseCode(HeaderDto header) {
        verifyResponseCode(header, KbcConstants.ResultCode.ZERO_NINE, "");
    }

    private void verifyResponseCode(
            HeaderDto header, final String expectedValue, String errorHeader) {
        String resultValue = getResultCodeOrThrow(header);
        if (!Objects.equals(expectedValue, resultValue)) {
            bankServerErrors(header, resultValue);
            notEnoughFundsCancelTransfer(header, resultValue);
            throwInvalidResultCodeError(header, resultValue, errorHeader);
        }
    }

    private void notEnoughFundsCancelTransfer(HeaderDto header, String resultValue) {
        if (resultValue.equalsIgnoreCase(KbcConstants.ResultCode.ZERO_TWO)
                && matchesErrorMessage(
                        header.getResultMessage(),
                        KbcConstants.ErrorMessage.ACCOUNT_HAS_INSUFFICIENT_FUNDS)) {
            cancelTransfer(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT.getKey().get());
        }
    }

    private void bankServerErrors(HeaderDto header, String resultValue) {
        if (resultValue.equalsIgnoreCase(KbcConstants.ResultCode.ZERO_TWO)
                && matchesErrorMessage(
                        header.getResultMessage(),
                        KbcConstants.ErrorMessage.THIRD_PARTY_SERVER_ERROR)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Error code: " + resultValue + ", message: " + header.getResultMessage());
        }
    }

    private void cancelTransfer(String message) throws TransferExecutionException {
        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(message)
                .setMessage(message)
                .build();
    }

    private static boolean matchesErrorMessage(TypeValuePair e, String errorMessage) {
        return e != null
                && e.getValue() != null
                && e.getValue().toLowerCase().contains(errorMessage);
    }

    private void throwInvalidResultCodeError(
            HeaderDto header, String resultCode, String errorHeader) {
        throw new IllegalStateException(
                formatInvalidResultCodeMessage(header, resultCode, errorHeader));
    }

    private void throwInvalidResultCodeError(HeaderDto header, String resultCode) {
        throw new IllegalStateException(formatInvalidResultCodeMessage(header, resultCode, ""));
    }

    private String formatInvalidResultCodeMessage(
            HeaderDto header, String resultCode, String errorHeader) {
        String resultMessage =
                Optional.ofNullable(header)
                        .map(HeaderDto::getResultMessage)
                        .map(TypeValuePair::getValue)
                        .orElse("");
        if (Strings.isNullOrEmpty(errorHeader)) {
            return String.format(
                    "Invalid result code - [ResultCode]: %s - [Message]: %s ",
                    resultCode, resultMessage);
        }

        return String.format(
                "Invalid result code - [ResultCode]: %s - [ErrorHeader]: %s - [Message]: %s ",
                resultCode, errorHeader == null ? "" : errorHeader, resultMessage);
    }

    // All responses are prepended with the iv used when encrypting the body.
    // This method will extract the iv from the response.
    private byte[] getCipherIv(byte[] cipherBytes) {
        return Arrays.copyOfRange(cipherBytes, 0, KbcConstants.Encryption.IV_LENGTH);
    }

    // Extract the body of the response containing the ciphertext
    private byte[] getCipherBody(byte[] cipherBytes) {
        return Arrays.copyOfRange(
                cipherBytes, KbcConstants.Encryption.IV_LENGTH, cipherBytes.length);
    }

    private String encryptAndEncodePublicKey(final byte[] cipherKey) {
        RSAPublicKey publicKey =
                RSA.getPubKeyFromBytes(
                        EncodingUtils.decodeBase64String(KbcConstants.Encryption.PUBLIC_KEY));
        byte[] cipherText = RSA.encryptNonePkcs1(publicKey, cipherKey);
        return EncodingUtils.encodeAsBase64String(cipherText);
    }

    private <T> String encryptAndEncodeRequest(T request, final byte[] cipherKey) {
        String serializedRequest = SerializationUtils.serializeToString(request);
        byte[] iv = RandomUtils.secureRandom(16);
        Preconditions.checkNotNull(serializedRequest);
        byte[] encryptedRequest = AES.encryptCbc(cipherKey, iv, serializedRequest.getBytes());
        byte[] concatenatedArrays = Bytes.concat(iv, encryptedRequest);
        return EncodingUtils.encodeAsBase64String(concatenatedArrays);
    }

    private <T> T decodeAndDecryptResponse(
            HttpResponse httpResponse, Class<T> responseClass, final byte[] cipherKey) {
        String responseBody = httpResponse.getBody(String.class);
        byte[] cipherBytes = EncodingUtils.decodeBase64String(responseBody);
        byte[] decryptedResponse =
                AES.decryptCbc(cipherKey, getCipherIv(cipherBytes), getCipherBody(cipherBytes));
        String response = new String(decryptedResponse, Charsets.UTF_8);
        logBody("Response body", response);
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
            KbcConstants.Url url, Object request, Class<T> responseType, final byte[] cipherKey) {
        return postStuff(
                url, request, responseType, DEFAULT_LANGUAGE_FOR_PARSE_ERROR_TEXTS, cipherKey);
    }

    private <T> T post(
            KbcConstants.Url url,
            Object request,
            Class<T> responseType,
            String requestLocale,
            final byte[] cipherKey) {
        return postStuff(url, request, responseType, requestLocale, cipherKey);
    }

    private <T> T postStuff(
            Url url,
            Object request,
            Class<T> responseType,
            String requestLocale,
            final byte[] cipherKey) {

        HttpResponse httpResponse = postRequest(url, request, true, requestLocale, cipherKey);

        return decodeAndDecryptResponse(httpResponse, responseType, cipherKey);
    }

    private HttpResponse postRequest(
            KbcConstants.Url url,
            Object request,
            boolean encryptAndEncodeRequest,
            String requestLocale,
            final byte[] cipherKey) {
        String completeRequest =
                request != null && encryptAndEncodeRequest
                        ? encryptAndEncodeRequest(request, cipherKey)
                        : SerializationUtils.serializeToString(request);
        logBody("Request body", SerializationUtils.serializeToString(request));
        return request != null
                ? client.request(url.get())
                        .header(KbcConstants.Headers.ACCEPT_LANG_KEY, requestLocale)
                        .post(HttpResponse.class, completeRequest)
                : client.request(url.get())
                        .header(KbcConstants.Headers.ACCEPT_LANG_KEY, requestLocale)
                        .post(HttpResponse.class);
    }

    private <T> Pair<T, String> postGetResponseAndHeader(
            KbcConstants.Url url, Object request, Class<T> responseType, final byte[] cipherKey) {
        return postGetResponseAndHeader(url, request, responseType, true, cipherKey);
    }

    private <T> Pair<T, String> postGetResponseAndHeader(
            Url url,
            Object request,
            Class<T> responseType,
            boolean encryptAndEncodeRequest,
            final byte[] cipherKey) {

        HttpResponse httpResponse =
                postRequest(
                        url,
                        request,
                        encryptAndEncodeRequest,
                        DEFAULT_LANGUAGE_FOR_PARSE_ERROR_TEXTS,
                        cipherKey);

        String headerValue = getHeaderValue(httpResponse);

        T response =
                encryptAndEncodeRequest
                        ? decodeAndDecryptResponse(httpResponse, responseType, cipherKey)
                        : cleanResponse(httpResponse, responseType);

        return new Pair<>(response, headerValue);
    }

    private String getHeaderValue(HttpResponse httpResponse) {
        if (httpResponse.getHeaders() != null
                && httpResponse.getHeaders().containsKey(KbcConstants.ErrorHeaders.LOGON_ERROR)) {
            return httpResponse.getHeaders().getFirst(KbcConstants.ErrorHeaders.LOGON_ERROR);
        }
        return "";
    }

    // == END PRIVATE METHODS ==

    public void prepareSession(final byte[] cipherKey) throws AuthorizationException {
        KeyExchangeRequest request =
                KeyExchangeRequest.createWithStandardTypes(
                        KbcConstants.RequestInput.COMPANY_ID,
                        KbcConstants.RequestInput.APP_FAMILY,
                        encryptAndEncodePublicKey(cipherKey));

        Pair<KeyExchangeResponse, String> response =
                postGetResponseAndHeader(
                        KbcConstants.Url.KEY_EXCHANGE,
                        request,
                        KeyExchangeResponse.class,
                        false,
                        cipherKey);
        checkBlockedAccount(response.first.getHeader(), response.second);
        verifyDoubleZeroResponseCode(response.first.getHeader());
    }

    public void logout(final byte[] cipherKey) {
        LogoutResponse response =
                post(KbcConstants.Url.LOGOUT, null, LogoutResponse.class, cipherKey);
        verifyDoubleZeroResponseCode(response.getHeader());
    }

    public String challenge(final byte[] cipherKey) throws AuthorizationException {
        ChallengeRequest challengeRequest =
                ChallengeRequest.createWithStandardTypes(
                        KbcConstants.RequestInput.AUTHENTICATION_TYPE,
                        DEFAULT_LANGUAGE_FOR_PARSE_ERROR_TEXTS);

        Pair<ChallengeResponse, String> response =
                postGetResponseAndHeader(
                        KbcConstants.Url.CHALLENGE,
                        challengeRequest,
                        ChallengeResponse.class,
                        cipherKey);
        checkBlockedAccount(response.first.getHeader(), response.second);
        verifyDoubleZeroResponseCode(response.first.getHeader());

        return response.first.getChallenge().getValue();
    }

    public void registerLogon(String username, String challengeResponse, final byte[] cipherKey)
            throws AuthorizationException {
        RegisterLogonRequest registerLogonRequest =
                RegisterLogonRequest.builder()
                        .applicationId(KbcConstants.ApplicationId.REGISTER_LOGON)
                        .captcha(KbcConstants.RequestInput.EMPTY_CAPTCHA)
                        .company(KbcConstants.RequestInput.COMPANY_ID)
                        .language(DEFAULT_LANGUAGE_FOR_PARSE_ERROR_TEXTS)
                        .response(challengeResponse)
                        .saveCardNumber(KbcConstants.RequestInput.SAVE_CARD_NUMBER)
                        .ucrType(KbcConstants.RequestInput.UCR_TYPE)
                        .username(username)
                        .build();

        Pair<RegisterLogonResponse, String> response =
                postGetResponseAndHeader(
                        KbcConstants.Url.REGISTER_LOGON,
                        registerLogonRequest,
                        RegisterLogonResponse.class,
                        cipherKey);
        checkBlockedAccount(response.first.getHeader(), response.second);
        verifyResponseCode(
                response.first.getHeader(), KbcConstants.ResultCode.DOUBLE_ZERO, response.second);
    }

    public String enrollDevice(final byte[] cipherKey) {
        EnrollDeviceRoundOneRequest enrollDeviceRequest =
                EnrollDeviceRoundOneRequest.createWithStandardTypes(
                        KbcConstants.RequestInput.IS_POLITICAL_PROMINENT_PERSION_CHECKED,
                        KbcConstants.ApplicationId.APPLICATION_TYPE_CODE,
                        KbcConstants.RequestInput.IS_COMMON_REPOST_STANDARD_CHECKED,
                        KbcConstants.RequestInput.APP_CONDITIONS_CHECKED,
                        KbcConstants.RequestInput.BANK_CONDITIONS_CHECKED,
                        KbcConstants.RequestInput.DOCCLE_CHECKED);

        EnrollDeviceRoundOneResponse response =
                post(
                        KbcConstants.Url.ENROLL_DEVICE,
                        enrollDeviceRequest,
                        EnrollDeviceRoundOneResponse.class,
                        cipherKey);
        verifyResponseCode(response.getHeader());

        return response.getHeader().getSigningId().getEncoded();
    }

    public String signTypeManual(String signingId, final byte[] cipherKey)
            throws AuthorizationException {
        SignRequest signRequest = SignRequest.createWithSigningId(signingId);

        Pair<SignTypesResponse, String> response =
                postGetResponseAndHeader(
                        KbcConstants.Url.SIGNING_TYPES,
                        signRequest,
                        SignTypesResponse.class,
                        cipherKey);
        checkBlockedAccount(response.first.getHeader(), response.second);
        verifyDoubleZeroResponseCode(response.first.getHeader());

        return response.first.getSignTypeId(KbcConstants.Predicates.SIGN_TYPE_MANUAL);
    }

    public String signChallenge(String signTypeId, String signingId, final byte[] cipherKey)
            throws AuthorizationException {
        SignChallengeRequest signChallengeRequest =
                SignChallengeRequest.create(signTypeId, signingId);

        Pair<SignChallengeResponse, String> response =
                postGetResponseAndHeader(
                        KbcConstants.Url.SIGNING_CHALLENGE,
                        signChallengeRequest,
                        SignChallengeResponse.class,
                        cipherKey);
        checkBlockedAccount(response.first.getHeader(), response.second);
        verifyDoubleZeroResponseCode(response.first.getHeader());

        return response.first.getChallenge().getValue();
    }

    public String signValidation(
            String signingResponse, String panNr, String signingId, final byte[] cipherKey)
            throws AuthorizationException {
        SignValidationRequest signValidationRequest =
                SignValidationRequest.create(signingResponse, panNr, signingId);

        Pair<SignValidationResponse, String> response =
                postGetResponseAndHeader(
                        KbcConstants.Url.SIGNING_VALIDATION,
                        signValidationRequest,
                        SignValidationResponse.class,
                        cipherKey);
        checkBlockedAccount(response.first.getHeader(), response.second);
        verifyDoubleZeroResponseCode(response.first.getHeader());

        return response.first.getHeader().getSigningId().getEncoded();
    }

    public EnrollDeviceRoundTwoResponse enrollDeviceWithSigningId(
            String signingId, final byte[] cipherKey)
            throws AuthorizationException, LoginException {
        EnrollDeviceRoundTwoRequest enrollDeviceRoundTwoRequest =
                EnrollDeviceRoundTwoRequest.create(signingId);

        Pair<EnrollDeviceRoundTwoResponse, String> response =
                postGetResponseAndHeader(
                        KbcConstants.Url.ENROLL_DEVICE,
                        enrollDeviceRoundTwoRequest,
                        EnrollDeviceRoundTwoResponse.class,
                        cipherKey);
        checkBlockedAccount(response.first.getHeader(), response.second);
        verifyResponseCodeForLogin(response.first.getHeader());

        return response.first;
    }

    public ActivationLicenseResponse activationLicence(
            KbcDevice device,
            String iv,
            String encryptedClientPublicKeyAndNonce,
            final byte[] cipherKey)
            throws AuthorizationException {
        ActivationLicenseRequest activationLicenseRequest =
                ActivationLicenseRequest.builder()
                        .applicationId(KbcConstants.RequestInput.APPLICATION_ID)
                        .applicationTypeCode(KbcConstants.ApplicationId.APPLICATION_TYPE_CODE)
                        .applicationVersionNo(KbcConstants.RequestInput.VERSION_NUMBER)
                        .clientInitialVector(iv)
                        .companyNo(KbcConstants.RequestInput.COMPANY_ID)
                        .deviceId(device.getDeviceId())
                        .encryptedClientPublicKeyAndNonce(encryptedClientPublicKeyAndNonce)
                        .language(DEFAULT_LANGUAGE_FOR_PARSE_ERROR_TEXTS)
                        .logonId(device.getAccessNumber())
                        .build();

        Pair<ActivationLicenseResponse, String> response =
                postGetResponseAndHeader(
                        KbcConstants.Url.ACTIVATION_LICENSE,
                        activationLicenseRequest,
                        ActivationLicenseResponse.class,
                        cipherKey);
        checkBlockedAccount(response.first.getHeader(), response.second);
        verifyDoubleZeroResponseCode(response.first.getHeader());

        return response.first;
    }

    public String activationInstance(
            KbcDevice device,
            String iv,
            String encryptedNonce,
            String challenge,
            String deviceCode,
            final byte[] cipherKey)
            throws AuthorizationException {
        ActivationInstanceRequest activationInstanceRequest =
                ActivationInstanceRequest.builder()
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
                        .language(DEFAULT_LANGUAGE_FOR_PARSE_ERROR_TEXTS)
                        .build();

        Pair<ActivationInstanceResponse, String> response =
                postGetResponseAndHeader(
                        KbcConstants.Url.ACTIVATION_INSTANCE,
                        activationInstanceRequest,
                        ActivationInstanceResponse.class,
                        cipherKey);
        checkBlockedAccount(response.first.getHeader(), response.second);
        verifyDoubleZeroResponseCode(response.first.getHeader());

        return response.first.getActivationMessage().getValue();
    }

    public void activationVerification(
            KbcDevice device, String verificationMessage, final byte[] cipherKey)
            throws AuthorizationException {
        ActivationVerificationRequest activationVerificationRequest =
                ActivationVerificationRequest.builder()
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
                        .language(DEFAULT_LANGUAGE_FOR_PARSE_ERROR_TEXTS)
                        .fingerprint(device.getFingerprint())
                        .build();

        Pair<ActivationVerificationResponse, String> response =
                postGetResponseAndHeader(
                        KbcConstants.Url.ACTIVATION_VERIFICATION,
                        activationVerificationRequest,
                        ActivationVerificationResponse.class,
                        cipherKey);
        checkBlockedAccount(response.first.getHeader(), response.second);
        verifyDoubleZeroResponseCode(response.first.getHeader());
    }

    public String challengeSotp(KbcDevice device, final byte[] cipherKey)
            throws AuthorizationException {
        ChallengeSotpRequest challengeRequest =
                ChallengeSotpRequest.builder()
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
                        .setLanguage(DEFAULT_LANGUAGE_FOR_PARSE_ERROR_TEXTS)
                        .build();

        Pair<ChallengeSotpResponse, String> response =
                postGetResponseAndHeader(
                        KbcConstants.Url.CHALLENGE_SOTP,
                        challengeRequest,
                        ChallengeSotpResponse.class,
                        cipherKey);
        checkBlockedAccount(response.first.getHeader(), response.second);
        verifyDoubleZeroResponseCode(response.first.getHeader());

        return response.first.getChallenge().getValue();
    }

    public void loginSotp(KbcDevice device, String otp, final byte[] cipherKey)
            throws AuthorizationException, LoginException {
        LoginSotpRequest loginSotpRequest =
                LoginSotpRequest.builder()
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
                        .setLanguage(DEFAULT_LANGUAGE_FOR_PARSE_ERROR_TEXTS)
                        .setFingerprint(device.getFingerprint())
                        .build();

        Pair<LoginSotpResponse, String> response =
                postGetResponseAndHeader(
                        KbcConstants.Url.LOGIN_SOTP,
                        loginSotpRequest,
                        LoginSotpResponse.class,
                        cipherKey);
        checkBlockedAccount(response.first.getHeader(), response.second);
        verifyResponseCodeForLogin(response.first.getHeader());
    }

    public AccountsResponse fetchAccounts(String language, final byte[] cipherKey) {
        AccountsRequest accountsRequest =
                AccountsRequest.builder()
                        .setBalanceIndicator(true)
                        .setIncludeReservationsIndicator(true)
                        .setIncludeAgreementMakeUp(true)
                        .setRetrieveSavingsAccountsOnlyIndicator(false)
                        .setRetrieveCurrentAccountsOnlyIndicator(false)
                        .setPaymentDashboardIndicator(true)
                        .build();

        this.accountResponse =
                post(
                        KbcConstants.Url.ACCOUNTS,
                        accountsRequest,
                        AccountsResponse.class,
                        language,
                        cipherKey);
        verifyDoubleZeroResponseCode(this.accountResponse.getHeader());

        return this.accountResponse;
    }

    public TransactionsHistoryResponse fetchTransactions(
            String accountNo, String repositioningKey, String language, final byte[] cipherKey) {
        Preconditions.checkNotNull(accountNo);

        AgreementDto targetAgreement =
                this.accountResponse.getAgreements().stream()
                        .filter(
                                agreementDto ->
                                        agreementDto.getAgreementNo().getValue().equals(accountNo))
                        .findFirst()
                        .orElseThrow(IllegalStateException::new);

        TransactionsHistoryRequest request =
                TransactionsHistoryRequest.builder()
                        .setAccountNo(targetAgreement.getAgreementNo())
                        .setRepositioningKey(
                                repositioningKey != null
                                        ? SerializationUtils.deserializeFromString(
                                                repositioningKey, TypeEncValueTuple.class)
                                        : null)
                        .setCompanyNo(targetAgreement.getCompanyNo())
                        .setCurrency(targetAgreement.getCurrency())
                        .setRoleCode(targetAgreement.getRoleCode())
                        .setSearchAmount(KbcConstants.RequestInput.SEARCH_AMOUNT)
                        .setSearchMessage(KbcConstants.RequestInput.SEARCH_MESSAGE)
                        .setTransactionsQuantity(KbcConstants.RequestInput.TRANSACTIONS_QUANTITY)
                        .build();
        TransactionsHistoryResponse response =
                post(
                        KbcConstants.Url.TRANSACTIONS_HISTORY,
                        request,
                        TransactionsHistoryResponse.class,
                        language,
                        cipherKey);
        verifyDoubleZeroResponseCode(response.getHeader());
        return response;
    }

    public FutureTransactionsResponse fetchFutureTransactions(
            String accountNo, String repositioningKey, final byte[] cipherKey) {
        Preconditions.checkNotNull(accountNo);

        FutureTransactionsRequest request =
                FutureTransactionsRequest.builder()
                        .setAccountNo(accountNo)
                        .setRepositioningKey(repositioningKey != null ? repositioningKey : "")
                        .setTransactionsQuantity(KbcConstants.RequestInput.TRANSACTIONS_QUANTITY)
                        .setCurrencyCode(KbcConstants.RequestInput.CURRENCY)
                        .build();

        FutureTransactionsResponse response =
                post(
                        KbcConstants.Url.FUTURE_TRANSACTIONS,
                        request,
                        FutureTransactionsResponse.class,
                        cipherKey);

        String resultCode = getResultCodeOrThrow(response.getHeader());
        if (!KbcConstants.ResultCode.DOUBLE_ZERO.equals(resultCode)
                && !KbcConstants.ResultCode.ZERO_TWO.equals(resultCode)) {
            throwInvalidResultCodeError(response.getHeader(), resultCode);
        }

        return response;
    }

    public String fetchCards(final byte[] cipherKey) {
        return post(KbcConstants.Url.CARDS, null, String.class, cipherKey);
    }

    public InvestmentPlansOverviewResponse fetchInvestmentPlanOverview(final byte[] cipherKey) {
        InvestmentPlansOverviewResponse response =
                post(
                        Url.INVESTMENT_PLAN_OVERVIEW,
                        null,
                        InvestmentPlansOverviewResponse.class,
                        cipherKey);
        verifyDoubleZeroResponseCode(response.getHeader());
        return response;
    }

    public InvestmentPlanDetailResponse fetchInvestmentPlanDetail(
            final InvestmentPlanDto investmentPlan, final byte[] cipherKey) {
        InvestmentPlanDetailResponse response =
                post(
                        Url.INVESTMENT_PLAN_DETAIL,
                        investmentPlan,
                        InvestmentPlanDetailResponse.class,
                        cipherKey);
        verifyDoubleZeroResponseCode(response.getHeader());
        return response;
    }

    public AssetsResponse fetchAssets(final byte[] cipherKey) {
        AssetsResponse response = post(Url.ASSETS, null, AssetsResponse.class, cipherKey);
        verifyDoubleZeroResponseCode(response.getHeader());
        return response;
    }

    public AssetsDetailResponse fetchAssetsDetail(
            final AssetJarsDto assetJarsDto, final byte[] cipherKey) {
        AssetsDetailResponse response =
                post(Url.ASSETS_DETAIL, assetJarsDto, AssetsDetailResponse.class, cipherKey);
        verifyDoubleZeroResponseCode(response.getHeader());
        return response;
    }

    private void logBody(String messageDescription, String body) {
        logger.info(
                messageDescription
                        + " (first 1000 chars):["
                        + body.substring(0, body.length() >= 1000 ? 1000 : body.length()));
    }
}
