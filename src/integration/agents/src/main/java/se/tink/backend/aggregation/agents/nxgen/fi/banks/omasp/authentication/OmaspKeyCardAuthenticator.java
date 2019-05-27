package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Objects;
import java.util.UUID;
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
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class OmaspKeyCardAuthenticator implements KeyCardAuthenticator {
    private static final AggregationLogger LOGGER =
            new AggregationLogger(OmaspKeyCardAuthenticator.class);

    private final OmaspApiClient apiClient;
    private final PersistentStorage persistentStorage;

    private SessionStorage sessionStorage;

    public OmaspKeyCardAuthenticator(
            OmaspApiClient apiClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
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
            if (httpResponse.getStatus() != 401 && httpResponse.getStatus() != 403) {
                throw e;
            }

            OmaspErrorResponse errorResponse = apiClient.getError(httpResponse);

            String error = errorResponse.getError();
            if (error == null) {
                throw e;
            }

            switch (error.toLowerCase()) {
                case OmaspConstants.Error.AUTHENTICATION_FAILED:
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
                case OmaspConstants.Error.OTHER_BANK_CUSTOMER:
                    throw LoginError.NOT_CUSTOMER.exception();
                case OmaspConstants.Error.LOGIN_WARNING:
                    String message = errorResponse.getMessage();

                    if (!isLoginBlocked(message)) {
                        throw e;
                    }

                    throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                            OmaspConstants.UserMessage.LOGIN_BLOCKED.getKey());
                default:
                    LOGGER.warn(
                            String.format(
                                    "%s: Unknown error code for loginRequest: %s, Message: %s",
                                    OmaspConstants.LogTags.LOG_TAG_AUTHENTICATION,
                                    errorResponse.getError(),
                                    errorResponse.getMessage()));
                    throw e;
            }
        }
    }

    @Override
    public void authenticate(String code) throws AuthenticationException, AuthorizationException {
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

            persistentStorage.put(
                    OmaspConstants.Storage.DEVICE_TOKEN, registerDeviceResponse.getDeviceToken());
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
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
                default:
                    LOGGER.warn(
                            String.format(
                                    "%s: Unknown error code for registerDevice: %s, Message: %s",
                                    OmaspConstants.LogTags.LOG_TAG_AUTHENTICATION,
                                    errorResponse.getError(),
                                    errorResponse.getMessage()));
                    throw e;
            }
        }
    }

    private boolean isLoginBlocked(String message) {
        return Objects.nonNull(message)
                && message.toLowerCase().contains(OmaspConstants.ErrorMessage.LOGIN_BLOCKED);
    }
}
