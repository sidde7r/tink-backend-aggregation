package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.UUID;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.StartAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.StartAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ValidateAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ValidateAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.utils.ArgentaCardNumber;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.utils.ArgentaSecurityUtil;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public class ArgentaAuthenticator implements MultiFactorAuthenticator, AutoAuthenticator {
    private ArgentaPersistentStorage persistentStorage;
    private ArgentaApiClient apiClient;
    private final Credentials credentials;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public ArgentaAuthenticator(
            ArgentaPersistentStorage persistentStorage,
            ArgentaApiClient apiClient,
            Credentials credentials,
            final SupplementalInformationHelper supplementalInformationHelper) {
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
        this.credentials = credentials;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        String deviceId = persistentStorage.getDeviceId();
        if (Strings.isNullOrEmpty(deviceId)) throw SessionError.SESSION_EXPIRED.exception();
        String cardNumber =
                ArgentaCardNumber.formatCardNumber(credentials.getField(Field.Key.USERNAME));

        ValidateAuthResponse validateAuthResponse = signInWithRegistredDevice(cardNumber, deviceId);
        storeUakAndHomeOffice(
                ArgentaSecurityUtil.getUak(
                        persistentStorage.getUak(),
                        validateAuthResponse.getUak(),
                        persistentStorage.getDeviceId()),
                validateAuthResponse.getHomeOfficeId());
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String deviceToken = persistentStorage.getDeviceId();
        String cardNumber =
                ArgentaCardNumber.formatCardNumber(credentials.getField(Field.Key.USERNAME));
        ValidateAuthResponse validateAuthResponse;
        if (Strings.isNullOrEmpty(deviceToken)) {
            validateAuthResponse = registerNewDevice(cardNumber);
            storeUakAndHomeOffice(
                    validateAuthResponse.getUak(), validateAuthResponse.getHomeOfficeId());
        }
        deviceToken = persistentStorage.getDeviceId();
        validateAuthResponse = signInWithRegistredDevice(cardNumber, deviceToken);
        storeUakAndHomeOffice(
                ArgentaSecurityUtil.getUak(
                        persistentStorage.getUak(),
                        validateAuthResponse.getUak(),
                        persistentStorage.getDeviceId()),
                validateAuthResponse.getHomeOfficeId());
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    private void storeUakAndHomeOffice(String uak, String homeOfficeId) {
        persistentStorage.storeUak(uak);
        persistentStorage.storeHomeOffice(homeOfficeId);
    }

    private StartAuthResponse startAuth(String username, String deviceId, boolean registered)
            throws LoginException, AuthorizationException {
        StartAuthRequest registrationRequest = new StartAuthRequest(username, registered);
        return apiClient.startAuth(ArgentaConstants.Url.AUTH_START, registrationRequest, deviceId);
    }

    private ValidateAuthResponse validateDevice(
            StartAuthResponse startAuthResponse, String username)
            throws SupplementalInfoException, LoginException, AuthorizationException {
        String twoFactorResponse =
                supplementalInformationHelper.waitForLoginChallengeResponse(
                        startAuthResponse.getChallenge());
        ValidateAuthRequest validateAuthRequest =
                new ValidateAuthRequest(
                        username, twoFactorResponse, ArgentaConstants.Api.AUTH_METHOD_REGISTER);
        return apiClient.validateAuth(validateAuthRequest, persistentStorage.getDeviceId());
    }

    private String generateRandomDeviceID() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    private ValidateAuthResponse signInWithRegistredDevice(String cardNumber, String deviceToken)
            throws SessionException {
        ValidateAuthResponse validateAuthResponse;
        StartAuthResponse startAuthResponse;
        try {
            startAuthResponse = startAuth(cardNumber, deviceToken, true);
            validateAuthResponse = validatePin(startAuthResponse, cardNumber);
            return validateAuthResponse;
        } catch (LoginException | AuthorizationException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private ValidateAuthResponse registerNewDevice(String cardNumber)
            throws SupplementalInfoException, LoginException, AuthorizationException {
        String deviceToken = generateRandomDeviceID();
        StartAuthResponse startAuthResponse = startAuth(cardNumber, deviceToken, false);
        persistentStorage.storeDeviceId(deviceToken);
        ValidateAuthResponse validateAuthResponse = validateDevice(startAuthResponse, cardNumber);
        return validateAuthResponse;
    }

    private ValidateAuthResponse validatePin(StartAuthResponse startAuthResponse, String cardNumber)
            throws SessionException, LoginException, AuthorizationException {
        if (!startAuthResponse
                .getAuthMethod()
                .equalsIgnoreCase(ArgentaConstants.Api.AUTH_METHOD_PIN)) {
            persistentStorage.storeDeviceId("");
            throw SessionError.SESSION_EXPIRED.exception();
        }

        String response =
                calculateResponse(startAuthResponse.getChallenge(), persistentStorage.getUak());

        return apiClient.validateAuth(
                new ValidateAuthRequest(cardNumber, response, ArgentaConstants.Api.AUTH_METHOD_PIN),
                persistentStorage.getDeviceId());
    }

    private String calculateResponse(String challenge, String uak) {
        return ArgentaSecurityUtil.generatePinResponseChallenge(challenge, uak);
    }
}
