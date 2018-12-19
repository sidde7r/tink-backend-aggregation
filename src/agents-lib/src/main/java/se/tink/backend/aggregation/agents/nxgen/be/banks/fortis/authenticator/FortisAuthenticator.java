package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator;

import com.google.common.base.Strings;
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
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.AuthenticationProcessRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.AuthenticationProcessResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.EBankingUsersRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.EbankingUsersResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.GenerateChallangeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.UserInfoResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.libraries.i18n.Catalog;

import java.util.Optional;

public class FortisAuthenticator implements MultiFactorAuthenticator, AutoAuthenticator {

    private final Catalog catalog;
    private final PersistentStorage persistentStorage;
    private final FortisApiClient apiClient;
    private final SupplementalInformationController supplementalInformationController;
    private static final AggregationLogger LOGGER = new AggregationLogger(FortisAuthenticator.class);

    public FortisAuthenticator(
            Catalog catalog,
            PersistentStorage persistentStorage,
            FortisApiClient apiClient,
            SupplementalInformationController supplementalInformationController) {
        this.catalog = catalog;
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
        this.supplementalInformationController = supplementalInformationController;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    private EbankingUsersResponse getEbankingUsers(
            String authenticationFactorId, String distributorId, String smid) {
        EBankingUsersRequest eBankingUsersRequest =
                new EBankingUsersRequest(
                        authenticationFactorId, distributorId, smid);
        return apiClient.getEBankingUsers(eBankingUsersRequest);
    }

    private AuthenticationProcessResponse createAuthenticationProcess(EBankingUserId eBankingUserId,
            String distributorId, String authMode) {

        AuthenticationProcessRequest authenticationProcessRequest =
                new AuthenticationProcessRequest(eBankingUserId, distributorId, authMode);

        AuthenticationProcessResponse authenticationProcessResponse =
                apiClient.createAuthenticationProcess(authenticationProcessRequest);

        return authenticationProcessResponse;
    }

    private void generateChallenges(String authenticationProcessID, String cardNumber, String smid, String agreementId,
            String deviceFingerprint)
            throws SupplementalInfoException, LoginException {
        GenerateChallangeRequest challangeRequest =
                new GenerateChallangeRequest(apiClient.getDistributorId(), authenticationProcessID);
        String challenge = apiClient.fetchChallenges(challangeRequest);

        String loginCode = waitForLoginCode(challenge);

        AuthResponse authResponse =
                AuthResponse.builder()
                        .withAuthProcId(authenticationProcessID)
                        .withAgreementId(agreementId)
                        .withAuthenticationMeanId(FortisConstants.HEADER_VALUES.AUTHENTICATION_DEVICE_PINNING)
                        .withCardNumber(cardNumber)
                        .withDistId(apiClient.getDistributorId())
                        .withSmid(smid)
                        .withChallenge(challenge)
                        .withResponse(loginCode)
                        .withDeviceFingerprint(deviceFingerprint)
                        .build();

        sendChallenges(authResponse);
    }

    private void sendChallenges(AuthResponse response) throws LoginException {

        HttpResponse httpres = null;
        try {
            httpres = apiClient.authenticationRequest(response.getUrlEncodedFormat());
        } catch (Exception e) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        String responseBody = httpres.getBody(String.class);

        if (!Strings.isNullOrEmpty(responseBody) && responseBody.contains(FortisConstants.ERRORCODE.ERROR_CODE)) {
            if (responseBody.contains(FortisConstants.ERRORCODE.INVALID_SIGNATURE_KO) || responseBody
                    .contains(FortisConstants.ERRORCODE.INVALID_SIGNATURE)) {
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            } else {
                throw new IllegalStateException(String.format("Unknown error: %s", responseBody));
            }
        }
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        String authenticatorFactorId = credentials.getField(Field.Key.USERNAME);
        String smid = credentials.getField(FortisConstants.FIELD.CLIENTNUMBER);
        String password = credentials.getField(Field.Key.PASSWORD);
        String deviceFingerprint = FortisUtils.calculateDeviceFingerPrint();

        persistentStorage.put(FortisConstants.STORAGE.ACCOUNT_PRODUCT_ID, authenticatorFactorId);
        persistentStorage.put(FortisConstants.STORAGE.SMID, smid);
        persistentStorage.put(FortisConstants.STORAGE.PASSWORD, password);
        persistentStorage.put(FortisConstants.STORAGE.DEVICE_FINGERPRINT, deviceFingerprint);

        Optional<EBankingUserId> ebankingUsersResponse = getEbankingUserId(authenticatorFactorId, smid);
        if (!ebankingUsersResponse.isPresent()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        String agreementId = ebankingUsersResponse.get().getAgreementId();

        if (Strings.isNullOrEmpty(agreementId)) {
            throw new IllegalArgumentException("agreementId cannot be null or empty!");
        }
        persistentStorage.put(FortisConstants.STORAGE.AGREEMENT_ID, agreementId);

        AuthenticationProcessResponse res = createAuthenticationProcess(ebankingUsersResponse.get(),
                apiClient.getDistributorId(),
                FortisConstants.HEADER_VALUES.AUTHENTICATION_DEVICE_PINNING);

        generateChallenges(res.getValue().getAuthenticationProcessId(), authenticatorFactorId, smid, agreementId,
                deviceFingerprint);
        getUserInfoAndPersistMuid();

        /*
            We are unable to verify the password the user provided in the device binding flow.
            For that reason we also initiate the flow used in autoAuthenticate()
         */
        if (!isCredentialsCorrect()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        getUserInfoAndPersistMuid();
    }

    private Optional<EBankingUserId> getEbankingUserId(String authenticatorFactorId, String smid) {
        EbankingUsersResponse eBankingUserIdEntity =
                getEbankingUsers(authenticatorFactorId, apiClient.getDistributorId(), smid);

        if (eBankingUserIdEntity.getValue() == null) {
            return Optional.empty();
        }

        if (eBankingUserIdEntity.getValue().getEBankingUsers().size() > 1) {
            LOGGER.warnExtraLong(String.format("authenticate, multiple users found: %s", ""),
                    FortisConstants.LOGTAG.MULTIPLE_USER_ENTITIES);
        }
        return Optional.ofNullable(
                eBankingUserIdEntity.getValue().getEBankingUsers().get(0).getEBankingUser().getEBankingUserId());
    }

    private void getUserInfoAndPersistMuid() throws LoginException, AuthorizationException {
        UserInfoResponse userInfoResponse;
        try {
            userInfoResponse = apiClient.getUserInfo();
            validateMuid(userInfoResponse);
        } catch (HttpClientException hce) {
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
        }

        persistentStorage.put(FortisConstants.STORAGE.MUID, userInfoResponse.getValue().getUserData().getMuid());
    }

    private void validateMuid(UserInfoResponse userInfoResponse) throws AuthorizationException {
        if (Strings.isNullOrEmpty(userInfoResponse.getValue().getUserData().getMuid())) {
            LOGGER.warnExtraLong(String.format("muid missing, muidcode %s", userInfoResponse.getValue().getUserData().getMuidCode()), FortisConstants.LOGTAG.LOGIN_ERROR);
            throw AuthorizationError.ACCOUNT_BLOCKED.exception();
        }
    }

    private boolean isCredentialsCorrect() {
        String muid = persistentStorage.get(FortisConstants.STORAGE.MUID);
        String password = persistentStorage.get(FortisConstants.STORAGE.PASSWORD);
        String agreementId = persistentStorage.get(FortisConstants.STORAGE.AGREEMENT_ID);

        String cardNumber = persistentStorage.get(FortisConstants.STORAGE.ACCOUNT_PRODUCT_ID);
        String smid = persistentStorage.get(FortisConstants.STORAGE.SMID);
        String deviceFingerprint = persistentStorage.get(FortisConstants.STORAGE.DEVICE_FINGERPRINT);

        Optional<EBankingUserId> ebankingUsersId = getEbankingUserId(cardNumber, smid);
        if (!ebankingUsersId.isPresent()) {
            return false;
        }

        AuthenticationProcessResponse res = createAuthenticationProcess(ebankingUsersId.get(),
                apiClient.getDistributorId(), FortisConstants.HEADER_VALUES.AUTHENTICATION_PASSWORD);
        String authenticationProcessId = res.getValue().getAuthenticationProcessId();

        GenerateChallangeRequest challangeRequest =
                new GenerateChallangeRequest(apiClient.getDistributorId(), authenticationProcessId);

        String challenge = apiClient.fetchChallenges(challangeRequest);

        String calculateChallenge = FortisUtils
                .calculateChallenge(muid, password, agreementId, challenge, authenticationProcessId);

        persistentStorage.put(FortisConstants.STORAGE.CALCULATED_CHALLENGE, calculateChallenge);

        AuthResponse authResponse =
                AuthResponse.builder()
                        .withAuthProcId(authenticationProcessId)
                        .withAgreementId(agreementId)
                        .withAuthenticationMeanId(FortisConstants.HEADER_VALUES.AUTHENTICATION_PASSWORD)
                        .withCardNumber(cardNumber)
                        .withDistId(apiClient.getDistributorId())
                        .withSmid(smid)
                        .withChallenge(challenge)
                        .withResponse(calculateChallenge)
                        .withDeviceFingerprint(deviceFingerprint)
                        .build();

        HttpResponse response = apiClient.authenticationRequest(authResponse.getUrlEncodedFormat());
        String responseBody = response.getBody(String.class);

        if (!Strings.isNullOrEmpty(responseBody) && responseBody.contains(FortisConstants.ERRORCODE.ERROR_CODE)) {
            if (responseBody.contains(FortisConstants.ERRORCODE.INVALID_SIGNATURE)) {
                return false;
            }
            LOGGER.warnExtraLong(responseBody, FortisConstants.LOGTAG.LOGIN_ERROR);
            return false;
        }
        return true;
    }

    @Override
    public void autoAuthenticate() throws SessionException, AuthorizationException {
        if (!isCredentialsCorrect()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        UserInfoResponse userInfoResponse;
        try {
            userInfoResponse = apiClient.getUserInfo();
            validateMuid(userInfoResponse);
        } catch (HttpClientException hce) {
            throw new IllegalStateException("Incorrect challenge in autoAuthenticate");
        }

        persistentStorage.put(FortisConstants.STORAGE.MUID, userInfoResponse.getValue().getUserData().getMuid());
    }

    private String waitForLoginCode(String challenge) throws SupplementalInfoException {
        return waitForSupplementalInformation(
                createDescriptionField(
                        catalog.getString(
                                "1. Insert your card into the card reader and press (M1)\n"
                                        + "2. 'CHALLENGE?' is displayed."
                                        + "Enter []"),
                        challenge),
                createInputField(
                        catalog.getString(
                                "3. 'PIN?' is displayed.\n"
                                        + "Enter your PIN and press (OK)\n"
                                        + "4. The e-signature is displayed.\n"
                                        + "Enter the e-signature")));
    }

    private String waitForSupplementalInformation(Field... fields)
            throws SupplementalInfoException {
        return supplementalInformationController
                .askSupplementalInformation(fields)
                .get("e-signature");
    }

    private Field createDescriptionField(String loginText, String challenge) {
        Field field = new Field();
        field.setMasked(false);
        field.setDescription(catalog.getString("Challenge"));
        field.setValue(challenge);
        field.setName("description");
        field.setHelpText(loginText);
        field.setSensitive(true);
        field.setImmutable(true);
        return field;
    }

    private Field createInputField(String loginText) {
        Field field = new Field();
        field.setMasked(false);
        field.setDescription(catalog.getString("Input"));
        field.setName("e-signature");
        field.setHelpText(loginText);
        field.setNumeric(true);
        field.setSensitive(true);
        return field;
    }
}
