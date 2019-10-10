package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator;

import com.google.common.base.Strings;
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
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.ExecuteContractUpdateResponse;
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
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.Catalog;

public class FortisAuthenticator implements MultiFactorAuthenticator, AutoAuthenticator {

    private final Catalog catalog;
    private final PersistentStorage persistentStorage;
    private final FortisApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private static final AggregationLogger LOGGER =
            new AggregationLogger(FortisAuthenticator.class);

    public FortisAuthenticator(
            Catalog catalog,
            PersistentStorage persistentStorage,
            FortisApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.catalog = catalog;
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
            throws SupplementalInfoException, LoginException, AuthorizationException {
        GenerateChallangeRequest challangeRequest =
                new GenerateChallangeRequest(apiClient.getDistributorId(), authenticationProcessID);
        String challenge = apiClient.fetchChallenges(challangeRequest);
        persistentStorage.put(FortisConstants.STORAGE.CHALLENGE, challenge);

        String loginCode = waitForLoginCode(challenge);
        if (Strings.isNullOrEmpty(loginCode) || !StringUtils.isNumeric(loginCode)) {
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
        }

        AuthResponse authResponse =
                AuthResponse.builder()
                        .withAuthProcId(authenticationProcessID)
                        .withAgreementId(agreementId)
                        .withAuthenticationMeanId(
                                FortisConstants.HEADER_VALUES.AUTHENTICATION_DEVICE_PINNING)
                        .withCardNumber(cardNumber)
                        .withDistId(apiClient.getDistributorId())
                        .withSmid(smid)
                        .withChallenge(challenge)
                        .withResponse(loginCode)
                        .withDeviceFingerprint(deviceFingerprint)
                        .withMeanId(FortisConstants.VALUES.UCR)
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
                && responseBody.contains(FortisConstants.ERRORCODE.ERROR_CODE)) {
            if (responseBody.contains(FortisConstants.ERRORCODE.INVALID_SIGNATURE)) {
                throw new IllegalStateException("Invalid signature");
            }
            if (responseBody.contains(FortisConstants.ERRORCODE.MAXIMUM_NUMBER_OF_TRIES)) {
                throw AuthorizationError.REACH_MAXIMUM_TRIES.exception();
            }
            if (responseBody.contains(FortisConstants.ERRORCODE.INVALID_SIGNATURE_KO)) {
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            } else {
                throw new IllegalStateException(String.format("Unknown error: %s", responseBody));
            }
        }
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        final String authenticatorFactorId = credentials.getField(Field.Key.USERNAME);
        final String smid = credentials.getField(FortisConstants.FIELD.CLIENTNUMBER);
        final String password = credentials.getField(Field.Key.PASSWORD);
        final String deviceFingerprint = FortisUtils.calculateDeviceFingerPrint();

        persistentStorage.put(FortisConstants.STORAGE.ACCOUNT_PRODUCT_ID, authenticatorFactorId);
        persistentStorage.put(FortisConstants.STORAGE.SMID, smid);
        persistentStorage.put(FortisConstants.STORAGE.PASSWORD, password);
        persistentStorage.put(FortisConstants.STORAGE.DEVICE_FINGERPRINT, deviceFingerprint);

        Optional<EBankingUserId> ebankingUsersResponse =
                getEbankingUserId(authenticatorFactorId, smid);
        if (!ebankingUsersResponse.isPresent()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        final String agreementId = ebankingUsersResponse.get().getAgreementId();

        if (Strings.isNullOrEmpty(agreementId)) {
            throw new IllegalArgumentException("agreementId cannot be null or empty!");
        }
        persistentStorage.put(FortisConstants.STORAGE.AGREEMENT_ID, agreementId);

        AuthenticationProcessResponse res =
                createAuthenticationProcess(
                        ebankingUsersResponse.get(),
                        apiClient.getDistributorId(),
                        FortisConstants.HEADER_VALUES.AUTHENTICATION_DEVICE_PINNING);

        generateChallenges(
                res.getValue().getAuthenticationProcessId(),
                authenticatorFactorId,
                smid,
                agreementId,
                deviceFingerprint);
        getUserInfoAndPersistMuid();

        final String muid = persistentStorage.get(FortisConstants.STORAGE.MUID);
        final String challenge = persistentStorage.get(FortisConstants.STORAGE.CHALLENGE);

        String calculateChallenge =
                FortisUtils.calculateChallenge(
                        muid,
                        password,
                        agreementId,
                        challenge,
                        res.getValue().getAuthenticationProcessId());
        persistentStorage.put(FortisConstants.STORAGE.CALCULATED_CHALLENGE, calculateChallenge);

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

            ExecuteContractUpdateResponse executeContractUpdateResponse =
                    apiClient.executeContractUpdate(
                            new ExecuteContractUpdateRequest(new SignatureEntity(token, signCode)));
        }
        if (!isCredentialsCorrect()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        getUserInfoAndPersistMuid();
    }

    private PrepareContractUpdateResponse prepareContractUpdate(String password) {

        if (Strings.isNullOrEmpty(password)) {
            password = FortisConstants.VALUES.PASSWORD;
        }
        return apiClient.prepareContractUpdate(
                new PrepareContractUpdateRequest(FortisConstants.VALUES.TCFLAG, password));
    }

    private Optional<EBankingUserId> getEbankingUserId(String authenticatorFactorId, String smid)
            throws LoginException {
        EbankingUsersResponse eBankingUserIdEntity =
                getEbankingUsers(authenticatorFactorId, apiClient.getDistributorId(), smid);

        if (eBankingUserIdEntity.getValue() == null) {
            return Optional.empty();
        }

        if (eBankingUserIdEntity.getValue().getEBankingUsers().size() > 1) {
            LOGGER.warnExtraLong(
                    String.format("authenticate, multiple users found: %s", ""),
                    FortisConstants.LOGTAG.MULTIPLE_USER_ENTITIES);
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
            LOGGER.warnExtraLong(
                    String.format("authenticate, no user data found: %s", ""),
                    FortisConstants.LOGTAG.NO_USER_DATA_FOUND);
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
                FortisConstants.STORAGE.MUID, userInfoResponse.getValue().getUserData().getMuid());
    }

    private void validateMuid(UserInfoResponse userInfoResponse) throws AuthorizationException {
        if (Strings.isNullOrEmpty(userInfoResponse.getValue().getUserData().getMuid())) {
            LOGGER.warnExtraLong(
                    String.format(
                            "muid missing, muidcode %s",
                            userInfoResponse.getValue().getUserData().getMuidCode()),
                    FortisConstants.LOGTAG.LOGIN_ERROR);
            throw AuthorizationError.DEVICE_LIMIT_REACHED.exception();
        }

        if (!Strings.isNullOrEmpty(userInfoResponse.getValue().getUserData().getMuidCode())
                && !FortisConstants.ERRORCODE.MUID_OK.equalsIgnoreCase(
                        userInfoResponse.getValue().getUserData().getMuidCode())) {
            LOGGER.warnExtraLong(
                    String.format(
                            "muidcode %s, daysPasswordStillValid %s",
                            userInfoResponse.getValue().getUserData().getMuidCode(),
                            userInfoResponse.getValue().getUserData().getDaysPasswordStillValid()),
                    FortisConstants.LOGTAG.LOGIN_ERROR);
        }
    }

    private boolean isCredentialsCorrect() throws LoginException {
        String muid =
                checkNotNullOrEmpty(
                        persistentStorage.get(FortisConstants.STORAGE.MUID),
                        FortisConstants.STORAGE.MUID);
        String agreementId =
                checkNotNullOrEmpty(
                        persistentStorage.get(FortisConstants.STORAGE.AGREEMENT_ID),
                        FortisConstants.STORAGE.AGREEMENT_ID);

        String cardNumber =
                checkNotNullOrEmpty(
                        persistentStorage.get(FortisConstants.STORAGE.ACCOUNT_PRODUCT_ID),
                        FortisConstants.STORAGE.ACCOUNT_PRODUCT_ID);
        String smid =
                checkNotNullOrEmpty(
                        persistentStorage.get(FortisConstants.STORAGE.SMID),
                        FortisConstants.STORAGE.SMID);
        String deviceFingerprint =
                checkNotNullOrEmpty(
                        persistentStorage.get(FortisConstants.STORAGE.DEVICE_FINGERPRINT),
                        FortisConstants.STORAGE.DEVICE_FINGERPRINT);

        String password =
                checkNotNullOrEmpty(
                        persistentStorage.get(FortisConstants.STORAGE.PASSWORD),
                        FortisConstants.STORAGE.PASSWORD);

        Optional<EBankingUserId> ebankingUsersId = getEbankingUserId(cardNumber, smid);
        if (!ebankingUsersId.isPresent()) {
            return false;
        }

        return true;
    }

    private String checkNotNullOrEmpty(String persistentStoreValue, String persistentStorageKey) {
        if (Strings.isNullOrEmpty(persistentStoreValue)) {
            String errorMessage =
                    String.format("PersistentStorage is missing %s", persistentStorageKey);
            LOGGER.warnExtraLong(errorMessage, FortisConstants.LOGTAG.LOGIN_ERROR);
            throw new IllegalStateException(errorMessage);
        }
        return persistentStoreValue;
    }

    @Override
    public void autoAuthenticate() throws SessionException, AuthorizationException {

        final String authenticatorFactorId =
                persistentStorage.get(FortisConstants.STORAGE.ACCOUNT_PRODUCT_ID);
        final String smid = persistentStorage.get(FortisConstants.STORAGE.SMID);
        final String agreementId = persistentStorage.get(FortisConstants.STORAGE.AGREEMENT_ID);
        final String password = persistentStorage.get(FortisConstants.STORAGE.PASSWORD);
        final String deviceFingerprint =
                persistentStorage.get(FortisConstants.STORAGE.DEVICE_FINGERPRINT);
        final String muid = persistentStorage.get(FortisConstants.STORAGE.MUID);

        if (Strings.isNullOrEmpty(password)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        EbankingUsersResponse ebankingUsersResponse =
                getEbankingUsers(authenticatorFactorId, apiClient.getDistributorId(), smid);

        if (ebankingUsersResponse.getValue().getEBankingUsers().size() != 0) {
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
                            FortisConstants.HEADER_VALUES.AUTHENTICATION_PASSWORD);

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
            persistentStorage.put(FortisConstants.STORAGE.CALCULATED_CHALLENGE, calculateChallenge);

            AuthResponse authResponse =
                    AuthResponse.builder()
                            .withAuthProcId(authenticationProcessId)
                            .withAgreementId(agreementId)
                            .withAuthenticationMeanId(
                                    FortisConstants.HEADER_VALUES.AUTHENTICATION_PASSWORD)
                            .withCardNumber(maskingCardNumber(authenticatorFactorId))
                            .withDistId(apiClient.getDistributorId())
                            .withSmid(smid)
                            .withChallenge(challenge)
                            .withResponse(calculateChallenge)
                            .withDeviceFingerprint(deviceFingerprint)
                            .withMeanId(FortisConstants.VALUES.DIDAP)
                            .build();

            UserInfoResponse userInfoResponse;
            try {
                sendChallenges(authResponse);
                userInfoResponse = apiClient.getUserInfo();
                validateMuid(userInfoResponse);

            } catch (HttpClientException | LoginException e) {
                throw new IllegalStateException("Incorrect challenge in autoAuthenticate", e);
            }
            persistentStorage.put(
                    FortisConstants.STORAGE.MUID,
                    userInfoResponse.getValue().getUserData().getMuid());
        } else {
            LOGGER.warnExtraLong(
                    String.format("authenticate, no user data found: %s", ""),
                    FortisConstants.LOGTAG.NO_USER_DATA_FOUND);
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private String waitForLoginCode(String challenge) throws SupplementalInfoException {
        return supplementalInformationHelper.waitForLoginChallengeResponse(challenge);
    }

    private String waitForSignCode(String challenge) throws SupplementalInfoException {
        return supplementalInformationHelper.waitForSignCodeChallengeResponse(challenge);
    }

    private String maskingCardNumber(String cardNumber) {
        StringBuilder cardString = new StringBuilder(cardNumber);
        for (int index = 6; index < 13; index++) {
            cardString.setCharAt(index, 'X');
        }
        return String.valueOf(cardString);
    }
}
