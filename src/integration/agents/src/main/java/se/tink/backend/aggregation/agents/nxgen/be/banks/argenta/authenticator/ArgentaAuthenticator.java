package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator;

import com.google.common.base.Strings;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaConstants.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ArgentaErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ConfigResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.StartAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.StartAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ValidateAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ValidateAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.utils.ArgentaSecurityUtil;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RequiredArgsConstructor
public class ArgentaAuthenticator implements TypedAuthenticator, AutoAuthenticator {
    private final ArgentaPersistentStorage persistentStorage;
    private final ArgentaApiClient apiClient;
    private final Credentials credentials;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final String aggregator;
    private final RandomValueGenerator randomValueGenerator;

    @Override
    public void autoAuthenticate() {
        String deviceId = persistentStorage.getDeviceId();
        if (Strings.isNullOrEmpty(deviceId)) throw SessionError.SESSION_EXPIRED.exception();
        String cardNumber = credentials.getField(Field.Key.USERNAME);

        ValidateAuthResponse validateAuthResponse =
                signInWithRegisteredDevice(cardNumber, deviceId);
        storeUakAndHomeOffice(
                ArgentaSecurityUtil.getUak(
                        persistentStorage.getUak(),
                        validateAuthResponse.getUak(),
                        persistentStorage.getDeviceId()),
                validateAuthResponse.getHomeOfficeId());
    }

    @Override
    public void authenticate(Credentials credentials) {
        String deviceToken = persistentStorage.getDeviceId();
        String cardNumber = credentials.getField(Field.Key.USERNAME);
        ValidateAuthResponse validateAuthResponse;
        if (Strings.isNullOrEmpty(deviceToken)) {
            validateAuthResponse = registerNewDevice(cardNumber);
            storeUakAndHomeOffice(
                    validateAuthResponse.getUak(), validateAuthResponse.getHomeOfficeId());
        }
        deviceToken = persistentStorage.getDeviceId();
        validateAuthResponse = signInWithRegisteredDevice(cardNumber, deviceToken);
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

    private ValidateAuthResponse registerNewDevice(String cardNumber) {
        String deviceToken = generateRandomDeviceID();
        persistentStorage.setNewCredential(true);
        ConfigResponse configResponse = mandatoryGetConfig(deviceToken);
        if (configResponse.isServiceNotAvailable()) {
            throw BankServiceError.NO_BANK_SERVICE.exception();
        }
        StartAuthResponse startAuthResponse = startAuth(cardNumber, deviceToken, false);
        persistentStorage.storeDeviceId(deviceToken);
        HttpResponse validateDeviceResponse = validateDevice(startAuthResponse, cardNumber);
        return validateSmsCodeIfNeeded(validateDeviceResponse, cardNumber)
                .orElseGet(() -> validateDeviceResponse.getBody(ValidateAuthResponse.class));
    }

    private Optional<ValidateAuthResponse> validateSmsCodeIfNeeded(
            HttpResponse validateDeviceResponse, String cardNumber) {
        if (validateDeviceResponse.getStatus() == 400
                && validateDeviceResponse.hasBody()
                && isErrorSigningStepRequired(validateDeviceResponse)) {
            return Optional.of(validateSmsCode(cardNumber));
        }
        return Optional.empty();
    }

    private boolean isErrorSigningStepRequired(HttpResponse validateDeviceResponse) {
        ArgentaErrorResponse argentaErrorResponse =
                validateDeviceResponse.getBody(ArgentaErrorResponse.class);
        return ErrorResponse.ERROR_SIGNING_STEPUP_REQUIRED.equalsIgnoreCase(
                argentaErrorResponse.getCode());
    }

    private ValidateAuthResponse signInWithRegisteredDevice(String cardNumber, String deviceToken) {
        ValidateAuthResponse validateAuthResponse;
        StartAuthResponse startAuthResponse;
        try {
            ConfigResponse configResponse = mandatoryGetConfig(deviceToken);
            if (configResponse.isServiceNotAvailable()) {
                throw BankServiceError.NO_BANK_SERVICE.exception();
            }
            startAuthResponse = startAuth(cardNumber, deviceToken, true);
            validateAuthResponse = validatePin(startAuthResponse, cardNumber);
            return validateAuthResponse;
        } catch (LoginException | AuthorizationException e) {
            throw SessionError.SESSION_EXPIRED.exception(e);
        }
    }

    private String generateRandomDeviceID() {
        return randomValueGenerator.getUUID().toString().toUpperCase();
    }

    private ConfigResponse mandatoryGetConfig(String deviceId) {
        return apiClient.getConfig(ArgentaConstants.Url.CONFIG, deviceId);
    }

    private StartAuthResponse startAuth(String username, String deviceId, boolean registered) {

        StartAuthRequest registrationRequest;

        if (persistentStorage.isNewCredential()) {
            registrationRequest = new StartAuthRequest(username, registered, aggregator);
        } else {
            registrationRequest = new StartAuthRequest(username, registered);
        }
        return apiClient.startAuth(ArgentaConstants.Url.AUTH_START, registrationRequest, deviceId);
    }

    private HttpResponse validateDevice(StartAuthResponse startAuthResponse, String username) {
        try {
            String twoFactorResponse =
                    supplementalInformationHelper.waitForLoginChallengeResponse(
                            startAuthResponse.getChallenge());
            ValidateAuthRequest validateAuthRequest =
                    new ValidateAuthRequest(
                            username, twoFactorResponse, ArgentaConstants.Api.AUTH_METHOD_REGISTER);
            return apiClient.validateAuth(validateAuthRequest, persistentStorage.getDeviceId());
        } catch (SupplementalInfoException supplementalInfoException) {
            if (!supplementalInfoException.getError().equals(SupplementalInfoError.NO_VALID_CODE)) {
                throw LoginError.ACTIVATION_TIMED_OUT.exception(supplementalInfoException);
            } else {
                throw supplementalInfoException;
            }
        }
    }

    private ValidateAuthResponse validateSmsCode(String cardNumber) {
        String challengeResponse = supplementalInformationHelper.waitForOtpInput();
        ValidateAuthRequest validateAuthRequest =
                new ValidateAuthRequest(
                        cardNumber, challengeResponse, ArgentaConstants.Api.AUTH_METHOD_SMS);
        return apiClient
                .validateAuth(validateAuthRequest, persistentStorage.getDeviceId())
                .getBody(ValidateAuthResponse.class);
    }

    private ValidateAuthResponse validatePin(
            StartAuthResponse startAuthResponse, String cardNumber) {
        if (!startAuthResponse
                .getAuthMethod()
                .equalsIgnoreCase(ArgentaConstants.Api.AUTH_METHOD_PIN)) {
            persistentStorage.storeDeviceId("");
            throw SessionError.SESSION_EXPIRED.exception();
        }

        String response =
                calculateResponse(startAuthResponse.getChallenge(), persistentStorage.getUak());

        return apiClient
                .validateAuth(
                        new ValidateAuthRequest(
                                cardNumber, response, ArgentaConstants.Api.AUTH_METHOD_PIN),
                        persistentStorage.getDeviceId())
                .getBody(ValidateAuthResponse.class);
    }

    private String calculateResponse(String challenge, String uak) {
        return ArgentaSecurityUtil.generatePinResponseChallenge(challenge, uak);
    }
}
