package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.UUID;

import se.tink.backend.aggregation.agents.exceptions.*;
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
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.libraries.i18n.Catalog;

public class ArgentaAuthenticator implements MultiFactorAuthenticator, AutoAuthenticator {
    private ArgentaPersistentStorage persistentStorage;
    private ArgentaApiClient apiClient;
    private SupplementalInformationController supplementalInformationController;
    private Catalog catalog;
    private final Credentials credentials;

    public ArgentaAuthenticator(
            ArgentaPersistentStorage persistentStorage,
            ArgentaApiClient apiClient,
            SupplementalInformationController supplementalInformationController,
            Catalog catalog,
            Credentials credentials) {
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
        this.supplementalInformationController = supplementalInformationController;
        this.catalog = catalog;
        this.credentials = credentials;
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
        String cardNumber = ArgentaCardNumber.formatCardNumber(credentials.getField(Field.Key.USERNAME));
        ValidateAuthResponse validateAuthResponse;
        if (Strings.isNullOrEmpty(deviceToken)) {
            validateAuthResponse = registerNewDevice(cardNumber);
            storeUakAndHomeOffice(validateAuthResponse.getUak(), validateAuthResponse.getHomeOfficeId());
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
            throws LoginException {
        StartAuthRequest registrationRequest = new StartAuthRequest(username, registered);
        return apiClient.startAuth(ArgentaConstants.Url.AUTH_START, registrationRequest, deviceId);
    }

    private ValidateAuthResponse validateDevice(StartAuthResponse startAuthResponse, String username)
            throws SupplementalInfoException, LoginException {
        String twoFactorResponse = getTwoFactorResponseRegistration(startAuthResponse.getChallenge());
        ValidateAuthRequest validateAuthRequest =
                new ValidateAuthRequest(username, twoFactorResponse, ArgentaConstants.Api.AUTH_METHOD_REGISTER);
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
        } catch (LoginException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private ValidateAuthResponse registerNewDevice(String cardNumber)
            throws SupplementalInfoException, LoginException {
        String deviceToken = generateRandomDeviceID();
        StartAuthResponse startAuthResponse = startAuth(cardNumber, deviceToken, false);
        persistentStorage.storeDeviceId(deviceToken);
        ValidateAuthResponse validateAuthResponse = validateDevice(startAuthResponse, cardNumber);
        return validateAuthResponse;
    }

    private ValidateAuthResponse validatePin(StartAuthResponse startAuthResponse, String cardNumber)
            throws SessionException, LoginException {
        if (!startAuthResponse
                .getAuthMethod()
                .equalsIgnoreCase(ArgentaConstants.Api.AUTH_METHOD_PIN)) {
            persistentStorage.storeDeviceId("");
            throw SessionError.SESSION_EXPIRED.exception();
        }

        String response = calculateResponse(startAuthResponse.getChallenge(), persistentStorage.getUak());

        return apiClient.validateAuth(
                new ValidateAuthRequest(cardNumber, response, ArgentaConstants.Api.AUTH_METHOD_PIN),
                persistentStorage.getDeviceId());
    }

    private String calculateResponse(String challenge, String uak) {
        return ArgentaSecurityUtil.generatePinResponseChallenge(challenge, uak);
    }

    private String getTwoFactorResponseRegistration(String challenge) throws SupplementalInfoException {
        return waitForSupplementalInformation(
                createDescriptionField(
                        catalog.getString(
                                "1$ Insert your debit card into your Digipass and press M1 \n"
                                        + "2$ Enter the challenge and press OK "),
                        challenge),
                createInputField(
                        catalog.getString("3$ Enter your PIN code and press OK\n"
                                        + "4$ Enter the response code here")));
    }

    private String waitForSupplementalInformation(Field... fields) throws SupplementalInfoException {
        return supplementalInformationController
                .askSupplementalInformation(fields)
                .get(ArgentaConstants.MultiFactorAuthentication.CODE);
    }

    private Field createDescriptionField(String helpText, String challenge) {
        Field field = new Field();
        field.setMasked(false);
        field.setDescription(challenge);
        field.setValue(challenge);
        field.setName("description");
        field.setHelpText(helpText);
        field.setImmutable(true);
        return field;
    }

    private Field createInputField(String helpText) {
        Field field = new Field();
        field.setMasked(false);
        field.setDescription(catalog.getString("Input"));
        field.setName(ArgentaConstants.MultiFactorAuthentication.CODE);
        field.setHelpText(helpText);
        field.setNumeric(true);
        field.setHint("NNNNNNN");
        return field;
    }
}
