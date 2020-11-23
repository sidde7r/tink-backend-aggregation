package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator;

import com.github.javafaker.Faker;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.responsevalidator.LoginResponseStatus;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.responsevalidator.LoginResponseValidator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.SendCardNumberResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.signature.BelfiusSignatureCreator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusIdGenerationUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusStringUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BelfiusAuthenticator implements PasswordAuthenticator, AutoAuthenticator {

    private static Logger logger = LoggerFactory.getLogger(BelfiusAuthenticator.class);

    private final BelfiusApiClient apiClient;
    private final Credentials credentials;
    private final PersistentStorage persistentStorage;
    private final BelfiusSessionStorage sessionStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final BelfiusSignatureCreator belfiusSignatureCreator;
    private final LoginResponseValidator loginResponseValidator;
    private final HumanInteractionDelaySimulator humanInteractionDelaySimulator;
    private boolean requestConfigIosSent;

    public BelfiusAuthenticator(
            final BelfiusApiClient apiClient,
            final Credentials credentials,
            final PersistentStorage persistentStorage,
            final BelfiusSessionStorage sessionStorage,
            final SupplementalInformationHelper supplementalInformationHelper,
            final BelfiusSignatureCreator belfiusSignatureCreator,
            final HumanInteractionDelaySimulator humanInteractionDelaySimulator) {
        this.apiClient = apiClient;
        this.credentials = credentials;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.belfiusSignatureCreator = belfiusSignatureCreator;
        this.humanInteractionDelaySimulator = humanInteractionDelaySimulator;
        this.loginResponseValidator = new LoginResponseValidator();
    }

    private static String generateModel() {
        final Faker faker = new Faker();
        return faker.name().firstName();
    }

    @Override
    public void authenticate(String panNumber, String password)
            throws AuthenticationException, AuthorizationException {

        apiClient.requestConfigIos();
        requestConfigIosSent = true;

        panNumber = BelfiusStringUtils.formatPanNumber(panNumber);

        String deviceToken = persistentStorage.get(BelfiusConstants.Storage.DEVICE_TOKEN);

        if (deviceToken == null
                || !isDeviceRegistered(panNumber, belfiusSignatureCreator.hash(deviceToken))) {
            deviceToken = BelfiusIdGenerationUtils.generateDeviceToken();
            registerDevice(panNumber, deviceToken);
        }

        final LoginResponseStatus loginResponseStatus = login(panNumber, password, deviceToken);
        handleManualLoginResponseErrors(loginResponseStatus);
    }

    @Override
    public void autoAuthenticate() throws SessionException, LoginException, AuthorizationException {

        if (!requestConfigIosSent) {
            apiClient.requestConfigIos();
            requestConfigIosSent = true;
        }

        String panNumber = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);
        String deviceToken = persistentStorage.get(BelfiusConstants.Storage.DEVICE_TOKEN);

        if (Strings.isNullOrEmpty(panNumber)
                || Strings.isNullOrEmpty(password)
                || Strings.isNullOrEmpty(deviceToken)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        panNumber = BelfiusStringUtils.formatPanNumber(panNumber);

        final LoginResponseStatus loginResponseStatus = loginAuto(panNumber, password, deviceToken);
        handleAutoLoginResponseErrors(loginResponseStatus);
    }

    private boolean isDeviceRegistered(String panNumber, String deviceToken) {
        apiClient.openSession();
        apiClient.startFlow();
        return apiClient.isDeviceRegistered(panNumber, deviceToken);
    }

    private void registerDevice(String panNumber, String deviceToken)
            throws AuthenticationException {

        // sleepForMilliseconds(5869); // Observed from app
        apiClient.openSession();
        apiClient.startFlow();

        apiClient.bacProductList();

        // sleepForMilliseconds(45724); // Observed from app
        apiClient.sendIsDeviceRegistered(panNumber, belfiusSignatureCreator.hash(deviceToken));

        // sleepForMilliseconds(2911); // Observed from app
        String challenge = apiClient.prepareAuthentication(panNumber);

        final String code =
                supplementalInformationHelper
                        .waitForLoginChallengeResponse(challenge)
                        .replace(" ", "");

        apiClient.keepAlive();

        humanInteractionDelaySimulator.delayExecution(500);

        apiClient.authenticateWithCode(code);

        final String deviceName = generateModel();

        logger.info("Belfius - Generated model name: {}", deviceName);

        // sleepForMilliseconds(195); // Observed from app

        apiClient.consultClientSettings();

        // sleepForMilliseconds(135); // Observed from app

        challenge =
                apiClient.prepareDeviceRegistration(
                        deviceToken, BelfiusConstants.BRAND, deviceName);

        final String sign =
                supplementalInformationHelper
                        .waitForSignCodeChallengeResponse(challenge)
                        .replace(" ", "");

        // Getting an error response here!!!!
        apiClient.registerDevice(sign);

        persistentStorage.put(BelfiusConstants.Storage.DEVICE_TOKEN, deviceToken);

        apiClient.closeSession(sessionStorage.getSessionId());
    }

    private LoginResponseStatus login(String panNumber, String password, String deviceToken)
            throws AuthenticationException {
        final String machineIdentifier = sessionStorage.getMachineIdentifier();

        apiClient.openSessionWithMachineIdentifier(machineIdentifier);
        apiClient.startFlow();

        PrepareLoginResponse response = apiClient.prepareLogin(panNumber);

        String contractNumber = response.getContractNumber();
        String challenge = response.getChallenge();

        // sessionStorage.setChallenge(challenge);

        String deviceTokenHashed = belfiusSignatureCreator.hash(deviceToken);
        String deviceTokenHashedIosComparison = belfiusSignatureCreator.hash(deviceTokenHashed);
        String signature =
                belfiusSignatureCreator.createSignatureSoft(challenge, deviceToken, panNumber);

        apiClient.bacProductList();

        final LoginResponseStatus loginResponseStatus =
                doLogin(deviceTokenHashed, deviceTokenHashedIosComparison, signature);
        if (loginResponseStatus != LoginResponseStatus.NO_ERRORS) {
            return loginResponseStatus;
        }

        apiClient.actorInformation();
        apiClient.closeSession(sessionStorage.getSessionId());

        apiClient.openSessionWithMachineIdentifier(machineIdentifier);
        apiClient.startFlow();

        SendCardNumberResponse sendCardNumberResponse = apiClient.sendCardNumber(panNumber);
        String challenge2 = sendCardNumberResponse.getChallenge();

        sessionStorage.setChallenge(challenge2);

        String signaturePw =
                belfiusSignatureCreator.createSignaturePw(
                        challenge2, deviceToken, panNumber, contractNumber, password);

        humanInteractionDelaySimulator.delayExecution(5000); // Entering password

        return doLoginPw(deviceTokenHashed, deviceTokenHashedIosComparison, signaturePw);
    }

    private LoginResponseStatus loginAuto(String panNumber, String password, String deviceToken)
            throws LoginException {
        apiClient.openSession();

        apiClient.startFlow();

        PrepareLoginResponse response = apiClient.prepareLogin(panNumber);

        String contractNumber = response.getContractNumber();

        String deviceTokenHashed = belfiusSignatureCreator.hash(deviceToken);
        String deviceTokenHashedIosComparison = belfiusSignatureCreator.hash(deviceTokenHashed);

        SendCardNumberResponse sendCardNumberResponse = apiClient.sendCardNumber(panNumber);
        String challenge2 = sendCardNumberResponse.getChallenge();

        sessionStorage.setChallenge(challenge2);

        humanInteractionDelaySimulator.delayExecution(5000); // Entering password

        String signaturePw =
                belfiusSignatureCreator.createSignaturePw(
                        challenge2, deviceToken, panNumber, contractNumber, password);

        return doLoginPw(deviceTokenHashed, deviceTokenHashedIosComparison, signaturePw);
    }

    private LoginResponseStatus doLogin(
            String deviceTokenHashed, String deviceTokenHashedIosComparison, String signature) {

        final LoginResponse loginResponse =
                apiClient.login(deviceTokenHashed, deviceTokenHashedIosComparison, signature);

        return loginResponseValidator.validate(loginResponse);
    }

    private LoginResponseStatus doLoginPw(
            String deviceTokenHashed, String deviceTokenHashedIosComparison, String signature) {

        final LoginResponse loginResponse =
                apiClient.loginPw(deviceTokenHashed, deviceTokenHashedIosComparison, signature);

        return loginResponseValidator.validate(loginResponse);
    }

    private static void handleManualLoginResponseErrors(LoginResponseStatus loginResponseStatus)
            throws AuthorizationException, LoginException {

        if (loginResponseStatus == LoginResponseStatus.ACCOUNT_BLOCKED) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception();
        } else if (loginResponseStatus == LoginResponseStatus.INCORRECT_CREDENTIALS) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        } else if (loginResponseStatus == LoginResponseStatus.SESSION_EXPIRED) {
            throw new IllegalArgumentException("Got session expired status during manual login.");
        }
    }

    private static void handleAutoLoginResponseErrors(LoginResponseStatus loginResponseStatus)
            throws AuthorizationException, LoginException, SessionException {

        if (loginResponseStatus == LoginResponseStatus.ACCOUNT_BLOCKED) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception();
        } else if (loginResponseStatus == LoginResponseStatus.INCORRECT_CREDENTIALS) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        } else if (loginResponseStatus == LoginResponseStatus.SESSION_EXPIRED) {
            throw SessionError.SESSION_EXPIRED.exception(
                    "SCA required do to an extended period of inactivity.");
        }
    }
}
