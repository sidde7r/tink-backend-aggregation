package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc;

import static se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants.DEFAULT_LANGUAGE_FOR_PARSE_ERROR_TEXTS;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.primitives.Bytes;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import org.assertj.core.util.Preconditions;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants.LogTags;
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
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto.SigningChallengeSotpResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto.SigningChallengeUcrResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto.TransferRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto.TransferResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto.ValidateTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AgreementDto;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.BeneficiariesResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.FutureTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.FutureTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.TransactionsHistoryRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.TransactionsHistoryResponse;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class KbcApiClient {
    private final TinkHttpClient client;
    private AccountsResponse accountResponse;
    private static final AggregationLogger LOGGER = new AggregationLogger(KbcApiClient.class);

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
            LOGGER.warnExtraLong(
                    String.format(
                            "Header: %s Error message:%s",
                            errorHeader, headerDto.getResultMessage()),
                    KbcConstants.LogTags.ERROR_CODE_MESSAGE);
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

    private void verifyResponseCode(HeaderDto header, final String expectedValue) {
        verifyResponseCode(header, expectedValue, "");
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
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
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
        PublicKey publicKey =
                RSA.getPubKeyFromBytes(
                        EncodingUtils.decodeBase64String(KbcConstants.Encryption.PUBLIC_KEY));
        byte[] cipherText = RSA.encryptNonePkcs1((RSAPublicKey) publicKey, cipherKey);
        return EncodingUtils.encodeAsBase64String(cipherText);
    }

    private <T> String encryptAndEncodeRequest(T request, final byte[] cipherKey) {
        String serializedRequest = SerializationUtils.serializeToString(request);
        byte[] iv = RandomUtils.secureRandom(16);
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
        // Uncomment to decrypted response in log LOGGER.infoExtraLong(response,
        // KbcConstants.LogTags.ACCOUNTS);
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
        return post(
                url,
                request,
                responseType,
                true,
                DEFAULT_LANGUAGE_FOR_PARSE_ERROR_TEXTS,
                cipherKey);
    }

    private <T> T post(
            KbcConstants.Url url,
            Object request,
            Class<T> responseType,
            String requestLocale,
            final byte[] cipherKey) {
        return post(url, request, responseType, true, requestLocale, cipherKey);
    }

    private <T> T post(
            KbcConstants.Url url,
            Object request,
            Class<T> responseType,
            boolean encryptAndEncodeRequest,
            String requestLocale,
            final byte[] cipherKey) {

        HttpResponse httpResponse =
                postRequest(url, request, encryptAndEncodeRequest, requestLocale, cipherKey);

        T response =
                encryptAndEncodeRequest
                        ? decodeAndDecryptResponse(httpResponse, responseType, cipherKey)
                        : cleanResponse(httpResponse, responseType);

        return response;
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
        return postGetResponseAndHeader(
                url, request, responseType, true, KbcConstants.ErrorHeaders.LOGON_ERROR, cipherKey);
    }

    private <T> Pair<T, String> postGetResponseAndHeader(
            KbcConstants.Url url,
            Object request,
            Class<T> responseType,
            boolean encryptAndEncodeRequest,
            String headerKey,
            final byte[] cipherKey) {

        HttpResponse httpResponse =
                postRequest(
                        url,
                        request,
                        encryptAndEncodeRequest,
                        DEFAULT_LANGUAGE_FOR_PARSE_ERROR_TEXTS,
                        cipherKey);

        String headerValue = getHeaderValue(headerKey, httpResponse);

        T response =
                encryptAndEncodeRequest
                        ? decodeAndDecryptResponse(httpResponse, responseType, cipherKey)
                        : cleanResponse(httpResponse, responseType);

        return new Pair<>(response, headerValue);
    }

    private String getHeaderValue(String headerKey, HttpResponse httpResponse) {
        if (httpResponse.getHeaders() != null && httpResponse.getHeaders().containsKey(headerKey)) {
            return httpResponse.getHeaders().getFirst(headerKey);
        }
        return "";
    }

    // == END PRIVATE METHODS ==

    public void prepareSession(final byte[] cipherKey) throws AuthorizationException {
        keyExchange(
                KbcConstants.RequestInput.COMPANY_ID,
                KbcConstants.RequestInput.APP_FAMILY,
                cipherKey);
    }

    public void logout(final byte[] cipherKey) {
        LogoutResponse response =
                post(KbcConstants.Url.LOGOUT, null, LogoutResponse.class, cipherKey);
        verifyDoubleZeroResponseCode(response.getHeader());
    }

    public KeyExchangeResponse keyExchange(
            String companyId, String appFamily, final byte[] cipherKey)
            throws AuthorizationException {
        KeyExchangeRequest request =
                KeyExchangeRequest.createWithStandardTypes(
                        companyId, appFamily, encryptAndEncodePublicKey(cipherKey));

        Pair<KeyExchangeResponse, String> response =
                postGetResponseAndHeader(
                        KbcConstants.Url.KEY_EXCHANGE,
                        request,
                        KeyExchangeResponse.class,
                        false,
                        KbcConstants.ErrorHeaders.LOGON_ERROR,
                        cipherKey);
        checkBlockedAccount(response.first.getHeader(), response.second);
        verifyDoubleZeroResponseCode(response.first.getHeader());

        return response.first;
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

    public RegisterLogonResponse registerLogon(
            String username, String challengeResponse, final byte[] cipherKey)
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

        return response.first;
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
        verifyResponseCode(response.getHeader(), KbcConstants.ResultCode.ZERO_NINE);

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

        LOGGER.info(String.format("%s postGetResponseAndHeader", LogTags.DEBUG));
        Pair<SignValidationResponse, String> response =
                postGetResponseAndHeader(
                        KbcConstants.Url.SIGNING_VALIDATION,
                        signValidationRequest,
                        SignValidationResponse.class,
                        cipherKey);
        LOGGER.info(String.format("%s checkBlockedAccount", LogTags.DEBUG));
        checkBlockedAccount(response.first.getHeader(), response.second);
        LOGGER.info(String.format("%s verifyDoubleZeroResponseCode", LogTags.DEBUG));
        verifyDoubleZeroResponseCode(response.first.getHeader());

        LOGGER.info(String.format("%s signValidation return", LogTags.DEBUG));
        return response.first.getHeader().getSigningId().getEncoded();
    }

    public EnrollDeviceRoundTwoResponse enrollDeviceWithSigningId(
            String signingId, final byte[] cipherKey) throws AuthorizationException {
        EnrollDeviceRoundTwoRequest enrollDeviceRoundTwoRequest =
                EnrollDeviceRoundTwoRequest.create(signingId);

        Pair<EnrollDeviceRoundTwoResponse, String> response =
                postGetResponseAndHeader(
                        KbcConstants.Url.ENROLL_DEVICE,
                        enrollDeviceRoundTwoRequest,
                        EnrollDeviceRoundTwoResponse.class,
                        cipherKey);
        checkBlockedAccount(response.first.getHeader(), response.second);
        verifyDoubleZeroResponseCode(response.first.getHeader());

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

    public ActivationVerificationResponse activationVerification(
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

        return response.first;
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

    public LoginSotpResponse loginSotp(KbcDevice device, String otp, final byte[] cipherKey)
            throws AuthorizationException {
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
        verifyDoubleZeroResponseCode(response.first.getHeader());

        return response.first;
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

    public AccountsResponse accountsForTransferToOwn(final byte[] cipherKey) {
        AccountsRequest request =
                AccountsRequest.builder()
                        .setIncludeAgreementMakeUp(true)
                        .setIncludeReservationsIndicator(true)
                        .build();

        AccountsResponse response =
                post(
                        KbcConstants.Url.ACCOUNTS_FOR_TRANSFER_TO_OWN,
                        request,
                        AccountsResponse.class,
                        cipherKey);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }

    public AccountsResponse accountsForTransferToOther(final byte[] cipherKey) {
        AccountsRequest request =
                AccountsRequest.builder()
                        .setIncludeAgreementMakeUp(true)
                        .setIncludeReservationsIndicator(true)
                        .build();

        AccountsResponse response =
                post(
                        KbcConstants.Url.ACCOUNTS_FOR_TRANSFER_TO_OTHER,
                        request,
                        AccountsResponse.class,
                        cipherKey);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }

    public BeneficiariesResponse beneficiariesHistory(final byte[] cipherKey) {
        BeneficiariesResponse response =
                post(
                        KbcConstants.Url.BENEFICIARIES_HISTORY,
                        null,
                        BeneficiariesResponse.class,
                        cipherKey);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }

    public ValidateTransferResponse validateTransfer(
            Transfer transfer,
            AgreementDto sourceAccount,
            boolean isTransferToOwnAccount,
            final byte[] cipherKey) {
        TransferRequest request =
                TransferRequest.create(transfer, sourceAccount, isTransferToOwnAccount);

        ValidateTransferResponse response =
                post(
                        KbcConstants.Url.TRANSFER_VALIDATE,
                        request,
                        ValidateTransferResponse.class,
                        cipherKey);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }

    public String prepareTransfer(
            Transfer transfer,
            boolean isTransferToOwnAccount,
            KbcConstants.Url url,
            AgreementDto sourceAccount,
            final byte[] cipherKey) {
        TransferRequest request =
                TransferRequest.create(transfer, sourceAccount, isTransferToOwnAccount);

        SignValidationResponse response =
                post(url, request, SignValidationResponse.class, cipherKey);
        verifyResponseCode(response.getHeader(), KbcConstants.ResultCode.ZERO_NINE);

        return response.getHeader().getSigningId().getEncoded();
    }

    public SignTypesResponse signingTypes(String signingId, final byte[] cipherKey) {
        SignRequest signRequest = SignRequest.createWithSigningId(signingId);

        SignTypesResponse signTypesResponse =
                post(
                        KbcConstants.Url.MOB_A031_SIGNING_TYPES,
                        signRequest,
                        SignTypesResponse.class,
                        cipherKey);
        verifyDoubleZeroResponseCode(signTypesResponse.getHeader());

        return signTypesResponse;
    }

    public SigningChallengeSotpResponse signingChallengeSotp(
            String signTypeId, String signingId, final byte[] cipherKey) {
        SignChallengeRequest request = SignChallengeRequest.create(signTypeId, signingId);

        SigningChallengeSotpResponse response =
                post(
                        KbcConstants.Url.MOB_A031_SIGNING_CHALLENGE_SOTP,
                        request,
                        SigningChallengeSotpResponse.class,
                        cipherKey);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }

    public void signingValidationSotp(
            String signingResponse, String panNr, String signingId, final byte[] cipherKey) {
        SignValidationRequest signValidationRequest =
                SignValidationRequest.create(signingResponse, panNr, signingId);

        SignValidationResponse signValidationResponse =
                post(
                        KbcConstants.Url.MOB_A031_SIGNING_VALIDATION_SOTP,
                        signValidationRequest,
                        SignValidationResponse.class,
                        cipherKey);
        verifyDoubleZeroResponseCode(signValidationResponse.getHeader());
    }

    public SigningChallengeUcrResponse signingChallengeUcr(
            String signTypeId, String signingId, final byte[] cipherKey) {
        SignChallengeRequest request = SignChallengeRequest.create(signTypeId, signingId);

        SigningChallengeUcrResponse response =
                post(
                        KbcConstants.Url.MOB_A031_SIGNING_CHALLENGE_UCR,
                        request,
                        SigningChallengeUcrResponse.class,
                        DEFAULT_LANGUAGE_FOR_PARSE_ERROR_TEXTS,
                        cipherKey);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }

    public void signingValidationUcr(
            String signingResponse, String panNr, String signingId, final byte[] cipherKey) {
        SignValidationRequest signValidationRequest =
                SignValidationRequest.create(signingResponse, panNr, signingId);

        SignValidationResponse signValidationResponse =
                post(
                        KbcConstants.Url.MOB_A031_SIGNING_VALIDATION_UCR,
                        signValidationRequest,
                        SignValidationResponse.class,
                        cipherKey);
        verifyDoubleZeroResponseCode(signValidationResponse.getHeader());
    }

    public TransferResponse signTransfer(
            String signingId, KbcConstants.Url url, final byte[] cipherKey) {
        SignRequest request = SignRequest.createWithSigningId(signingId);

        TransferResponse response =
                post(
                        url,
                        request,
                        TransferResponse.class,
                        DEFAULT_LANGUAGE_FOR_PARSE_ERROR_TEXTS,
                        cipherKey);
        verifyDoubleZeroResponseCode(response.getHeader());

        return response;
    }
}
