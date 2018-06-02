package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.MessageResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.MessageResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusSecurityUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusStringUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.onetimecode.OneTimeActivationCodeAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

public class BelfiusAuthenticator implements PasswordAuthenticator, AutoAuthenticator {

    private final BelfiusApiClient apiClient;
    private final Credentials credentials;
    private final PersistentStorage persistentStorage;
    private final SupplementalInformationController supplementalInformationController;
    private final BelfiusSessionStorage sessionStorage;

    public BelfiusAuthenticator(
            BelfiusApiClient apiClient,
            Credentials credentials,
            PersistentStorage persistentStorage,
            SupplementalInformationController supplementalInformationController,
            BelfiusSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.credentials = credentials;
        this.persistentStorage = persistentStorage;
        this.supplementalInformationController = supplementalInformationController;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String panNumber, String password) throws AuthenticationException, AuthorizationException {
        panNumber = BelfiusStringUtils.formatPanNumber(panNumber);

        String deviceToken = persistentStorage.get(BelfiusConstants.Storage.DEVICE_TOKEN);

        if (deviceToken == null || !isDeviceRegistered(panNumber, BelfiusSecurityUtils.hash(deviceToken))) {
            deviceToken = BelfiusSecurityUtils.generateDeviceToken();
            registerDevice(panNumber, deviceToken);
        }

        login(panNumber, password, deviceToken);
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        String panNumber = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);
        String deviceToken = persistentStorage.get(BelfiusConstants.Storage.DEVICE_TOKEN);

        if (Strings.isNullOrEmpty(panNumber) || Strings.isNullOrEmpty(password) || Strings.isNullOrEmpty(deviceToken)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        panNumber = BelfiusStringUtils.formatPanNumber(panNumber);

        try {
            login(panNumber, password, deviceToken);
        } catch (AuthenticationException | AuthorizationException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private boolean isDeviceRegistered(String panNumber, String deviceToken) {
        apiClient.openSession();
        apiClient.startFlow();
        return apiClient.isDeviceRegistered(panNumber, deviceToken);
    }

    private void registerDevice(String panNumber, String deviceToken)
            throws AuthenticationException, AuthorizationException {
        apiClient.openSession();
        apiClient.startFlow();

        String challenge = apiClient.prepareAuthentication(panNumber);
        String code = waitForLoginCode(challenge);
        apiClient.authenticateWithCode(code);

        String deviceBrand = BelfiusConstants.Device.DEVICE_BRAND;
        String deviceName = BelfiusConstants.Device.DEVICE_NAME;

        challenge = apiClient.prepareDeviceRegistration(deviceToken, deviceBrand, deviceName);
        String sign = waitForSignCode(challenge);
        BelfiusResponse belfiusResponse = apiClient.registerDevice(sign);
        persistentStorage.put(BelfiusConstants.Storage.DEVICE_TOKEN, deviceToken);
    }

    private void login(String panNumber, String password, String deviceToken)
            throws AuthenticationException, AuthorizationException {
        apiClient.openSession();
        apiClient.startFlow();

        PrepareLoginResponse response = apiClient.prepareLogin(panNumber);
        String contractNumber = response.getContractNumber();
        String challenge = response.getChallenge();

        sessionStorage.setChallenge(challenge);

        String deviceTokenHashed = BelfiusSecurityUtils.hash(deviceToken);
        String deviceTokenHashedIosComparison = BelfiusSecurityUtils.hash(deviceTokenHashed);
        String signature = BelfiusSecurityUtils.createSignature(
                challenge, deviceToken, panNumber, contractNumber, password);

        apiClient.login(deviceTokenHashed, deviceTokenHashedIosComparison, signature);
    }

    private String waitForLoginCode(String challenge) throws SupplementalInfoException {
        return waitForSupplementalInformation("Insert your card into the card reader & press LOGIN. "
                + "Enter the control code and your pin code in the card reader. "
                + "Then enter the code from the card reader in the field below.", challenge);
    }

    private String waitForSignCode(String challenge) throws SupplementalInfoException {
        return waitForSupplementalInformation("Insert your card into the card reader & press SIGN. "
                        + "Enter the control code and your pin code in the card reader. "
                        + "Then enter the code from the card reader in the field below.", challenge);
    }

    private String waitForSupplementalInformation(String helpText, String controlCode)
            throws SupplementalInfoException {
        return supplementalInformationController.askSupplementalInformation(
                createDescriptionField(helpText, controlCode),
                createInputField(BelfiusConstants.MultiFactorAuthentication.CODE))
                .get(BelfiusConstants.MultiFactorAuthentication.CODE);
    }

    private Field createDescriptionField(String description, String challenge) {
        Field field = new Field();
        field.setMasked(false);
        field.setDescription("Control Code");
        field.setName("description");
        field.setHelpText(description);
        field.setValue(challenge);
        field.setImmutable(true);
        return field;
    }

    private Field createInputField(String name) {
        Field field = new Field();
        field.setMasked(false);
        field.setDescription("Response Code");
        field.setName(name);
        field.setNumeric(true);
        field.setHint("NNNNNNN");
        return field;
    }
}
