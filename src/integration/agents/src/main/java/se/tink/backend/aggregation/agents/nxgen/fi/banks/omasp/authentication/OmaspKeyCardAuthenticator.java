package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Objects;
import java.util.UUID;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.OmaspApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.OmaspConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.OmaspConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.entities.SecurityKeyIndexEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.rpc.RegisterDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc.OmaspErrorResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class OmaspKeyCardAuthenticator implements KeyCardAuthenticator {
    private static final AggregationLogger logger =
            new AggregationLogger(OmaspKeyCardAuthenticator.class);

    private final OmaspApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;
    private SessionStorage sessionStorage;

    public OmaspKeyCardAuthenticator(
            OmaspApiClient apiClient,
            PersistentStorage persistentStorage,
            Credentials credentials,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public KeyCardInitValues init(String username, String password)
            throws AuthenticationException, AuthorizationException {
        try {
            LoginResponse loginResponse = apiClient.login(username, password);

            Preconditions.checkState(
                    loginResponse.getSecurityKeyRequired(),
                    "Code card authentication not required! (it should be)");

            SecurityKeyIndexEntity securityKeyEntity =
                    Preconditions.checkNotNull(
                            loginResponse.getSecurityKeyIndex(),
                            "No code card information in response");
            Preconditions.checkState(
                    !Strings.isNullOrEmpty(loginResponse.getSecurityKeyIndex().getIndex()),
                    "No code card information in response (but entity exists)");

            sessionStorage.put(Storage.FULL_NAME, loginResponse.getName());

            return new KeyCardInitValues(securityKeyEntity.getIndex());
        } catch (HttpResponseException e) {
            HttpResponse httpResponse = e.getResponse();
            if (httpResponse.getStatus() != HttpStatus.SC_UNAUTHORIZED
                    && httpResponse.getStatus() != HttpStatus.SC_FORBIDDEN
                    && httpResponse.getStatus() != HttpStatus.SC_BAD_REQUEST) {
                throw e;
            }

            OmaspErrorResponse errorResponse = apiClient.getError(httpResponse);

            String error = errorResponse.getError();
            if (error == null) {
                throw e;
            }

            switch (error.toLowerCase()) {
                case OmaspConstants.Error.AUTHENTICATION_FAILED:
                    throw LoginError.INCORRECT_CREDENTIALS.exception(e);
                case OmaspConstants.Error.BAD_REQUEST:
                    if (errorResponse.isPasswordError()) {
                        throw LoginError.INCORRECT_CREDENTIALS.exception(e);
                    } else {
                        throw e;
                    }
                case OmaspConstants.Error.OTHER_BANK_CUSTOMER:
                    throw LoginError.NOT_CUSTOMER.exception(e);
                case OmaspConstants.Error.LOGIN_WARNING:
                    String message = errorResponse.getMessage();

                    if (!isLoginBlocked(message)) {
                        throw e;
                    }

                    throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                            OmaspConstants.UserMessage.LOGIN_BLOCKED.getKey(), e);
                default:
                    logger.warn(
                            String.format(
                                    "%s: Unknown error code for loginRequest: %s, Message: %s",
                                    OmaspConstants.LogTags.LOG_TAG_AUTHENTICATION,
                                    errorResponse.getError(),
                                    errorResponse.getMessage()),
                            e);
                    throw e;
            }
        }
    }

    @Override
    public void authenticate(String code) throws AuthenticationException, AuthorizationException {
        credentials.setSensitivePayload(Field.Key.OTP_INPUT, code);
        try {
            String deviceId = persistentStorage.get(Storage.DEVICE_ID);

            // The Omasp App generates a random UUID if none is present
            if (Strings.isNullOrEmpty(deviceId)) {
                deviceId = UUID.randomUUID().toString().toUpperCase();
                persistentStorage.put(Storage.DEVICE_ID, deviceId);
            }

            RegisterDeviceResponse registerDeviceResponse =
                    apiClient.registerDevice(persistentStorage.get(Storage.DEVICE_ID), code);

            Preconditions.checkArgument(
                    !Strings.isNullOrEmpty(registerDeviceResponse.getDeviceToken()),
                    "Device token is null or empty");

            String deviceToken = registerDeviceResponse.getDeviceToken();
            credentials.setSensitivePayload(Storage.DEVICE_TOKEN, deviceToken);
            persistentStorage.put(OmaspConstants.Storage.DEVICE_TOKEN, deviceToken);

        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() != 403) {
                throw e;
            }

            OmaspErrorResponse errorResponse = apiClient.getError(e.getResponse());

            String error = errorResponse.getError();
            if (error == null) {
                throw e;
            }

            switch (error.toLowerCase()) {
                case OmaspConstants.Error.SECURITY_KEY_FAILED:
                    throw LoginError.INCORRECT_CREDENTIALS.exception(e);
                default:
                    logger.warn(
                            String.format(
                                    "%s: Unknown error code for registerDevice: %s, Message: %s",
                                    OmaspConstants.LogTags.LOG_TAG_AUTHENTICATION,
                                    errorResponse.getError(),
                                    errorResponse.getMessage()),
                            e);
                    throw e;
            }
        }
    }

    private boolean isLoginBlocked(String message) {
        return Objects.nonNull(message)
                && message.toLowerCase().contains(OmaspConstants.ErrorMessage.LOGIN_BLOCKED);
    }
}
