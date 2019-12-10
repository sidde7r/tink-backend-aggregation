package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator;

import com.google.common.base.Strings;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.SendCardNumberResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusSecurityUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusStringUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BelfiusAuthenticator implements PasswordAuthenticator, AutoAuthenticator {

    private final BelfiusApiClient apiClient;
    private final Credentials credentials;
    private final PersistentStorage persistentStorage;
    private final BelfiusSessionStorage sessionStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final String aggregator;
    private boolean requestConfigIosSent;

    public BelfiusAuthenticator(
            final BelfiusApiClient apiClient,
            final Credentials credentials,
            final PersistentStorage persistentStorage,
            final BelfiusSessionStorage sessionStorage,
            final SupplementalInformationHelper supplementalInformationHelper,
            final String aggregator) {
        this.apiClient = apiClient;
        this.credentials = credentials;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.aggregator = aggregator;
    }

    @Override
    public void authenticate(String panNumber, String password)
            throws AuthenticationException, AuthorizationException {

        apiClient.requestConfigIos();
        requestConfigIosSent = true;

        panNumber = BelfiusStringUtils.formatPanNumber(panNumber);

        String deviceToken = persistentStorage.get(BelfiusConstants.Storage.DEVICE_TOKEN);

        if (deviceToken == null
                || !isDeviceRegistered(panNumber, BelfiusSecurityUtils.hash(deviceToken))) {
            deviceToken = BelfiusSecurityUtils.generateDeviceToken();
            registerDevice(panNumber, deviceToken);
        }

        login(panNumber, password, deviceToken);
    }

    @Override
    public void autoAuthenticate() throws SessionException {

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

        try {
            loginAuto(panNumber, password, deviceToken);
        } catch (AuthenticationException | AuthorizationException e) {
            throw SessionError.SESSION_EXPIRED.exception(e);
        }
    }

    private boolean isDeviceRegistered(String panNumber, String deviceToken) {
        apiClient.openSession();
        apiClient.startFlow();
        return apiClient.isDeviceRegistered(panNumber, deviceToken);
    }

    private void registerDevice(String panNumber, String deviceToken)
            throws AuthenticationException, AuthorizationException {

        sleepForMilliseconds(5869); // Observed from app
        apiClient.openSession();
        apiClient.startFlow();

        apiClient.bacProductList();

        sleepForMilliseconds(45724); // Observed from app
        apiClient.sendIsDeviceRegistered(panNumber, BelfiusSecurityUtils.hash(deviceToken));

        sleepForMilliseconds(2911); // Observed from app
        String challenge = apiClient.prepareAuthentication(panNumber);

        final String code =
                supplementalInformationHelper
                        .waitForLoginChallengeResponse(challenge)
                        .replace(" ", "");

        apiClient.keepAlive();

        sleepForMilliseconds(500);

        apiClient.authenticateWithCode(code);

        final String deviceBrand = aggregator;
        final String deviceName = BelfiusConstants.MODEL;

        sleepForMilliseconds(195); // Observed from app

        apiClient.consultClientSettings();

        sleepForMilliseconds(135); // Observed from app

        challenge = apiClient.prepareDeviceRegistration(deviceToken, deviceBrand, deviceName);

        final String sign =
                supplementalInformationHelper
                        .waitForSignCodeChallengeResponse(challenge)
                        .replace(" ", "");

        // Getting an error response here!!!!
        apiClient.registerDevice(sign);

        persistentStorage.put(BelfiusConstants.Storage.DEVICE_TOKEN, deviceToken);

        apiClient.closeSession(sessionStorage.getSessionId());
    }

    private void login(String panNumber, String password, String deviceToken)
            throws AuthenticationException, AuthorizationException {
        final String machineIdentifier = sessionStorage.getMachineIdentifier();

        apiClient.openSessionWithMachineIdentifier(machineIdentifier);
        apiClient.startFlow();

        PrepareLoginResponse response = apiClient.prepareLogin(panNumber);

        String contractNumber = response.getContractNumber();
        String challenge = response.getChallenge();

        // sessionStorage.setChallenge(challenge);

        String deviceTokenHashed = BelfiusSecurityUtils.hash(deviceToken);
        String deviceTokenHashedIosComparison = BelfiusSecurityUtils.hash(deviceTokenHashed);
        String signature =
                BelfiusSecurityUtils.createSignatureSoft(
                        challenge, deviceToken, panNumber, contractNumber, password);

        apiClient.bacProductList();
        apiClient.login(deviceTokenHashed, deviceTokenHashedIosComparison, signature);
        apiClient.actorInformation();
        apiClient.closeSession(sessionStorage.getSessionId());

        apiClient.openSessionWithMachineIdentifier(machineIdentifier);
        apiClient.startFlow();

        SendCardNumberResponse sendCardNumberResponse = apiClient.sendCardNumber(panNumber);
        String challenge2 = sendCardNumberResponse.getChallenge();

        sessionStorage.setChallenge(challenge2);

        String signaturePw =
                BelfiusSecurityUtils.createSignaturePw(
                        challenge2, deviceToken, panNumber, contractNumber, password);

        sleepForMilliseconds(5000); // Entering password

        apiClient.loginPw(deviceTokenHashed, deviceTokenHashedIosComparison, signaturePw);
    }

    private void loginAuto(String panNumber, String password, String deviceToken)
            throws AuthenticationException, AuthorizationException {
        final String machineIdentifier = sessionStorage.getMachineIdentifier();

        apiClient.openSessionWithMachineIdentifier(machineIdentifier);

        apiClient.startFlow();

        PrepareLoginResponse response = apiClient.prepareLogin(panNumber);

        String contractNumber = response.getContractNumber();

        String deviceTokenHashed = BelfiusSecurityUtils.hash(deviceToken);
        String deviceTokenHashedIosComparison = BelfiusSecurityUtils.hash(deviceTokenHashed);

        SendCardNumberResponse sendCardNumberResponse = apiClient.sendCardNumber(panNumber);
        String challenge2 = sendCardNumberResponse.getChallenge();

        sessionStorage.setChallenge(challenge2);

        sleepForMilliseconds(5000); // Entering password

        String signaturePw =
                BelfiusSecurityUtils.createSignaturePw(
                        challenge2, deviceToken, panNumber, contractNumber, password);

        apiClient.loginPw(deviceTokenHashed, deviceTokenHashedIosComparison, signaturePw);
    }

    private static void sleepForMilliseconds(final int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}
