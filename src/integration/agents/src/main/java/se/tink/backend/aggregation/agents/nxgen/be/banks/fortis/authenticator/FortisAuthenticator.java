package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.AuthResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.EBankingUserId;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.ExecuteContractUpdateRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.PrepareContractUpdateRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.PrepareContractUpdateResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.SignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.AuthenticationProcessRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.AuthenticationProcessResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.EBankingUsersRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.EbankingUsersResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.GenerateChallangeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.UserInfoResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class FortisAuthenticator implements TypedAuthenticator, AutoAuthenticator {

    private final PersistentStorage persistentStorage;
    private final FortisApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SupplementalInformationFormer supplementalInformationFormer;
    private static final AggregationLogger logger =
            new AggregationLogger(FortisAuthenticator.class);

    public FortisAuthenticator(
            CredentialsRequest credentialsRequest,
            PersistentStorage persistentStorage,
            FortisApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.supplementalInformationFormer =
                new SupplementalInformationFormer(credentialsRequest.getProvider());
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    private EbankingUsersResponse getEbankingUsers(
            String authenticationFactorId, String distributorId, String smid) {
        EBankingUsersRequest eBankingUsersRequest =
                new EBankingUsersRequest(
                        authenticationFactorId, distributorId, smid, FortisConstants.CARD_FRAME_ID);
        return apiClient.getEBankingUsers(eBankingUsersRequest);
    }

    private AuthenticationProcessResponse createAuthenticationProcess(
            EBankingUserId eBankingUserId, String distributorId, String authMode) {

        AuthenticationProcessRequest authenticationProcessRequest =
                new AuthenticationProcessRequest(eBankingUserId, distributorId, authMode);

        AuthenticationProcessResponse authenticationProcessResponse =
                apiClient.createAuthenticationProcess(authenticationProcessRequest);

        return authenticationProcessResponse;
    }

    private void generateChallenges(
            String authenticationProcessID,
            String cardNumber,
            String smid,
            String agreementId,
            String deviceFingerprint)
            throws SupplementalInfoException, LoginException, AuthorizationException,
                    SessionException {
        GenerateChallangeRequest challangeRequest =
                new GenerateChallangeRequest(apiClient.getDistributorId(), authenticationProcessID);
        String challenge = apiClient.fetchChallenges(challangeRequest);
        persistentStorage.put(FortisConstants.Storage.CHALLENGE, challenge);

        String loginCode = waitForLoginCode(challenge);
        if (Strings.isNullOrEmpty(loginCode) || !StringUtils.isNumeric(loginCode)) {
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
        }

        AuthResponse authResponse =
                AuthResponse.builder()
                        .withAuthProcId(authenticationProcessID)
                        .withAgreementId(agreementId)
                        .withAuthenticationMeanId(
                                FortisConstants.HeaderValues.AUTHENTICATION_DEVICE_PINNING)
                        .withCardNumber(cardNumber)
                        .withDistId(apiClient.getDistributorId())
                        .withSmid(smid)
                        .withChallenge(challenge)
                        .withResponse(loginCode)
                        .withDeviceFingerprint(deviceFingerprint)
                        .withMeanId(FortisConstants.Values.UCR)
                        .build();

        sendChallenges(authResponse);
    }

    private void sendChallenges(AuthResponse response)
            throws LoginException, AuthorizationException {
        HttpResponse httpResponse;
        try {
            httpResponse = apiClient.authenticationRequest(response.getUrlEncodedFormat());
        } catch (Exception e) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(e);
        }

        String responseBody = httpResponse.getBody(String.class);

        if (!Strings.isNullOrEmpty(responseBody)
                && responseBody.contains(FortisConstants.ErrorCode.ERROR_CODE)) {
            clearAuthenticationData();
            if (responseBody.contains(FortisConstants.ErrorCode.INVALID_SIGNATURE)) {
                throw LoginError.PASSWORD_CHANGED.exception();
            } else if (responseBody.contains(FortisConstants.ErrorCode.MAXIMUM_NUMBER_OF_TRIES)) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception();
            } else if (responseBody.contains(FortisConstants.ErrorCode.INVALID_SIGNATURE_KO)) {
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            } else if (responseBody.contains(
                    FortisConstants.ErrorCode.COMBINATION_HARDWARE_ID_AND_LOGIN_ID_NOT_FOUND)) {

                throw LoginError.REGISTER_DEVICE_ERROR.exception();
            } else {
                throw new IllegalStateException(String.format("Unknown error: %s", responseBody));
            }
        }
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        logger.info("Trying to authenticate manually...");
        final String authenticatorFactorId =
                credentials.getField(se.tink.backend.agents.rpc.Field.Key.USERNAME);
        final String smid = credentials.getField(FortisConstants.Field.CLIENTNUMBER);
        final String password = waitForPassword();
        final String deviceFingerprint = FortisUtils.calculateDeviceFingerPrint();

        persistentStorage.put(FortisConstants.Storage.ACCOUNT_PRODUCT_ID, authenticatorFactorId);
        persistentStorage.put(FortisConstants.Storage.SMID, smid);
        persistentStorage.put(FortisConstants.Storage.PASSWORD, password);
        persistentStorage.put(FortisConstants.Storage.DEVICE_FINGERPRINT, deviceFingerprint);

        Optional<EBankingUserId> ebankingUsersResponse =
                getEbankingUserId(authenticatorFactorId, smid);
        if (!ebankingUsersResponse.isPresent()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        final String agreementId = ebankingUsersResponse.get().getAgreementId();

        if (Strings.isNullOrEmpty(agreementId)) {
            throw new IllegalArgumentException("agreementId cannot be null or empty!");
        }
        persistentStorage.put(FortisConstants.Storage.AGREEMENT_ID, agreementId);

        AuthenticationProcessResponse res =
                createAuthenticationProcess(
                        ebankingUsersResponse.get(),
                        apiClient.getDistributorId(),
                        FortisConstants.HeaderValues.AUTHENTICATION_DEVICE_PINNING);

        generateChallenges(
                res.getValue().getAuthenticationProcessId(),
                authenticatorFactorId,
                smid,
                agreementId,
                deviceFingerprint);
        getUserInfoAndPersistMuid();

        final String muid = persistentStorage.get(FortisConstants.Storage.MUID);
        final String challenge = persistentStorage.get(FortisConstants.Storage.CHALLENGE);

        String calculateChallenge =
                FortisUtils.calculateChallenge(
                        muid,
                        password,
                        agreementId,
                        challenge,
                        res.getValue().getAuthenticationProcessId());
        persistentStorage.put(FortisConstants.Storage.CALCULATED_CHALLENGE, calculateChallenge);

        PrepareContractUpdateResponse prepareContractUpdateResponse =
                prepareContractUpdate(password);

        if (!(prepareContractUpdateResponse.getValue().getChallenge().getChallenges().size()
                == 0)) {
            final String signChallenge =
                    prepareContractUpdateResponse.getValue().getChallenge().getChallenges().get(0);
            final String token = prepareContractUpdateResponse.getValue().getChallenge().getToken();

            String signCode = waitForSignCode(signChallenge);
            if (Strings.isNullOrEmpty(signCode) || !StringUtils.isNumeric(signCode)) {
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            }

            apiClient.executeContractUpdate(
                    new ExecuteContractUpdateRequest(new SignatureEntity(token, signCode)));
        }
        if (!isCredentialsCorrect()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        getUserInfoAndPersistMuid();
        persistentStorage.put(FortisConstants.Storage.MANUAL_AUTHENTICATION_REQUIRED, false);
    }

    private PrepareContractUpdateResponse prepareContractUpdate(String password) {
        if (Strings.isNullOrEmpty(password)) {
            password = FortisConstants.Values.PASSWORD;
        }
        return apiClient.prepareContractUpdate(
                new PrepareContractUpdateRequest(FortisConstants.Values.TCFLAG, password));
    }

    private Optional<EBankingUserId> getEbankingUserId(String authenticatorFactorId, String smid)
            throws LoginException {
        EbankingUsersResponse eBankingUserIdEntity =
                getEbankingUsers(authenticatorFactorId, apiClient.getDistributorId(), smid);

        if (eBankingUserIdEntity.getValue() == null) {
            return Optional.empty();
        }

        if (eBankingUserIdEntity.getValue().getEBankingUsers().size() > 1) {
            logger.warnExtraLong(
                    String.format("authenticate, multiple users found: %s", ""),
                    FortisConstants.LoggingTag.MULTIPLE_USER_ENTITIES);
        }

        if (eBankingUserIdEntity.getValue().getEBankingUsers().size() != 0) {
            return Optional.ofNullable(
                    eBankingUserIdEntity
                            .getValue()
                            .getEBankingUsers()
                            .get(0)
                            .getEBankingUser()
                            .getEBankingUserId());
        } else {
            logger.warnExtraLong(
                    String.format("authenticate, no user data found: %s", ""),
                    FortisConstants.LoggingTag.NO_USER_DATA_FOUND);
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    private void getUserInfoAndPersistMuid() throws LoginException, AuthorizationException {
        UserInfoResponse userInfoResponse;
        try {
            userInfoResponse = apiClient.getUserInfo();
            validateMuid(userInfoResponse);
        } catch (HttpClientException hce) {
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(hce);
        }
        persistentStorage.put(
                FortisConstants.Storage.MUID, userInfoResponse.getValue().getUserData().getMuid());
    }

    private void validateMuid(UserInfoResponse userInfoResponse) throws AuthorizationException {
        if (Strings.isNullOrEmpty(userInfoResponse.getValue().getUserData().getMuid())) {
            logger.warnExtraLong(
                    String.format(
                            "muid missing, muidcode %s",
                            userInfoResponse.getValue().getUserData().getMuidCode()),
                    FortisConstants.LoggingTag.LOGIN_ERROR);
            throw AuthorizationError.DEVICE_LIMIT_REACHED.exception();
        }

        if (!Strings.isNullOrEmpty(userInfoResponse.getValue().getUserData().getMuidCode())
                && !FortisConstants.ErrorCode.MUID_OK.equalsIgnoreCase(
                        userInfoResponse.getValue().getUserData().getMuidCode())) {
            logger.warnExtraLong(
                    String.format(
                            "muidcode %s, daysPasswordStillValid %s",
                            userInfoResponse.getValue().getUserData().getMuidCode(),
                            userInfoResponse.getValue().getUserData().getDaysPasswordStillValid()),
                    FortisConstants.LoggingTag.LOGIN_ERROR);
        }
    }

    private boolean isCredentialsCorrect() throws LoginException {
        checkNotNullOrEmpty(
                persistentStorage.get(FortisConstants.Storage.MUID), FortisConstants.Storage.MUID);
        checkNotNullOrEmpty(
                persistentStorage.get(FortisConstants.Storage.AGREEMENT_ID),
                FortisConstants.Storage.AGREEMENT_ID);
        checkNotNullOrEmpty(
                persistentStorage.get(FortisConstants.Storage.DEVICE_FINGERPRINT),
                FortisConstants.Storage.DEVICE_FINGERPRINT);
        checkNotNullOrEmpty(
                persistentStorage.get(FortisConstants.Storage.PASSWORD),
                FortisConstants.Storage.PASSWORD);

        String cardNumber =
                checkNotNullOrEmpty(
                        persistentStorage.get(FortisConstants.Storage.ACCOUNT_PRODUCT_ID),
                        FortisConstants.Storage.ACCOUNT_PRODUCT_ID);
        String smid =
                checkNotNullOrEmpty(
                        persistentStorage.get(FortisConstants.Storage.SMID),
                        FortisConstants.Storage.SMID);
        return getEbankingUserId(cardNumber, smid).isPresent();
    }

    private String checkNotNullOrEmpty(String persistentStoreValue, String persistentStorageKey) {
        if (Strings.isNullOrEmpty(persistentStoreValue)) {
            String errorMessage =
                    String.format("PersistentStorage is missing %s", persistentStorageKey);
            logger.warnExtraLong(errorMessage, FortisConstants.LoggingTag.LOGIN_ERROR);
            throw new IllegalStateException(errorMessage);
        }
        return persistentStoreValue;
    }

    @Override
    public void autoAuthenticate() throws SessionException, AuthorizationException, LoginException {

        final String authenticatorFactorId =
                persistentStorage.get(FortisConstants.Storage.ACCOUNT_PRODUCT_ID);
        final String smid = persistentStorage.get(FortisConstants.Storage.SMID);
        final String agreementId = persistentStorage.get(FortisConstants.Storage.AGREEMENT_ID);
        final String password = persistentStorage.get(FortisConstants.Storage.PASSWORD);
        final String deviceFingerprint =
                persistentStorage.get(FortisConstants.Storage.DEVICE_FINGERPRINT);
        final String muid = persistentStorage.get(FortisConstants.Storage.MUID);

        logger.info(
                "Password is null/empty (during auto auth): " + Strings.isNullOrEmpty(password));

        if (Strings.isNullOrEmpty(password) || Strings.isNullOrEmpty(muid)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        logger.info("Trying to authenticate automatic....");
        EbankingUsersResponse ebankingUsersResponse =
                getEbankingUsers(authenticatorFactorId, apiClient.getDistributorId(), smid);

        if (ebankingUsersResponse.getValue() != null
                && ebankingUsersResponse.getValue().getEBankingUsers().size() != 0) {
            Optional<EBankingUserId> eBankingUserId =
                    Optional.ofNullable(
                            ebankingUsersResponse
                                    .getValue()
                                    .getEBankingUsers()
                                    .get(0)
                                    .getEBankingUser()
                                    .getEBankingUserId());

            AuthenticationProcessResponse res =
                    createAuthenticationProcess(
                            eBankingUserId.get(),
                            apiClient.getDistributorId(),
                            FortisConstants.HeaderValues.AUTHENTICATION_PASSWORD);

            final String authenticationProcessId = res.getValue().getAuthenticationProcessId();

            GenerateChallangeRequest challengeRequest =
                    new GenerateChallangeRequest(
                            apiClient.getDistributorId(), authenticationProcessId);

            final String challenge = apiClient.fetchChallenges(challengeRequest);

            final String calculateChallenge =
                    FortisUtils.calculateChallenge(
                            muid,
                            password,
                            agreementId,
                            challenge,
                            res.getValue().getAuthenticationProcessId());
            persistentStorage.put(FortisConstants.Storage.CALCULATED_CHALLENGE, calculateChallenge);

            AuthResponse authResponse =
                    AuthResponse.builder()
                            .withAuthProcId(authenticationProcessId)
                            .withAgreementId(agreementId)
                            .withAuthenticationMeanId(
                                    FortisConstants.HeaderValues.AUTHENTICATION_PASSWORD)
                            .withCardNumber(maskingCardNumber(authenticatorFactorId))
                            .withDistId(apiClient.getDistributorId())
                            .withSmid(smid)
                            .withChallenge(challenge)
                            .withResponse(calculateChallenge)
                            .withDeviceFingerprint(deviceFingerprint)
                            .withMeanId(FortisConstants.Values.DIDAP)
                            .build();

            UserInfoResponse userInfoResponse;
            sendChallenges(authResponse);
            userInfoResponse = apiClient.getUserInfo();
            validateMuid(userInfoResponse);
            persistentStorage.put(
                    FortisConstants.Storage.MUID,
                    userInfoResponse.getValue().getUserData().getMuid());
        } else {
            logger.warnExtraLong(
                    String.format("authenticate, no user data found: %s", ""),
                    FortisConstants.LoggingTag.NO_USER_DATA_FOUND);
            clearAuthenticationData();
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private String waitForPassword() throws SupplementalInfoException, SessionException {
        try {
            return supplementalInformationHelper
                    .askSupplementalInformation(
                            supplementalInformationFormer.getField(Field.Key.PASSWORD))
                    .get(Field.Key.PASSWORD.getFieldKey());
        } catch (SupplementalInfoException ex) {
            // there was timeout on waiting for supplement information
            // should be handled as session expired
            logger.info("Timeout on waiting for the Password supplemental information");
            throw SessionError.SESSION_EXPIRED.exception(ex);
        }
    }

    private String waitForLoginCode(String challenge)
            throws SupplementalInfoException, SessionException {
        try {
            return supplementalInformationHelper.waitForLoginChallengeResponse(challenge);
        } catch (SupplementalInfoException ex) {
            // there was timeout on waiting for supplement information
            // should be handled as session expired
            logger.info("Timeout on waiting for the Login Code supplemental information");
            throw SessionError.SESSION_EXPIRED.exception(ex);
        }
    }

    private String waitForSignCode(String challenge) throws SessionException {
        try {
            return supplementalInformationHelper.waitForSignCodeChallengeResponse(challenge);
        } catch (SupplementalInfoException ex) {
            // there was timeout on waiting for supplement information
            // should be handled as session expired
            logger.info("Timeout on waiting for the Sign Code supplemental information");
            throw SessionError.SESSION_EXPIRED.exception(ex);
        }
    }

    private String maskingCardNumber(String cardNumber) {
        StringBuilder cardString = new StringBuilder(cardNumber);
        for (int index = 6; index < 13; index++) {
            cardString.setCharAt(index, 'X');
        }
        return String.valueOf(cardString);
    }

    private void clearAuthenticationData() {
        ImmutableList<String> valuesToClean =
                ImmutableList.of(
                        FortisConstants.Storage.ACCOUNT_PRODUCT_ID,
                        FortisConstants.Storage.SMID,
                        FortisConstants.Storage.AGREEMENT_ID,
                        FortisConstants.Storage.PASSWORD,
                        FortisConstants.Storage.DEVICE_FINGERPRINT,
                        FortisConstants.Storage.MUID);
        valuesToClean.forEach(v -> persistentStorage.put(v, null));
        logger.info(
                "Password is null/empty (after cleaning): "
                        + Strings.isNullOrEmpty(
                                persistentStorage.get(FortisConstants.Storage.PASSWORD)));
        persistentStorage.put(FortisConstants.Storage.MANUAL_AUTHENTICATION_REQUIRED, true);
    }
}
