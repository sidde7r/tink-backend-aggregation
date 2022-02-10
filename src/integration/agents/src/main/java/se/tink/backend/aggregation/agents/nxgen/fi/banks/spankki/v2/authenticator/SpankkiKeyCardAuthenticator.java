package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.entities.CustomerEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc.KeyCardLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc.UserPasswordLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.utils.SpankkiAuthUtils;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.entities.StatusEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class SpankkiKeyCardAuthenticator implements KeyCardAuthenticator {
    private final SpankkiApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    public SpankkiKeyCardAuthenticator(
            SpankkiApiClient apiClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public KeyCardInitValues init(String username, String password)
            throws AuthenticationException, AuthorizationException {
        final SpankkiAuthUtils authUtils = new SpankkiAuthUtils(apiClient);
        authUtils.solveChallenge();
        try {
            final String pinPosition =
                    apiClient
                            .userPasswordLogin(username, password)
                            .getLoginStatusEntity()
                            .getPinPosition();

            return new KeyCardInitValues(pinPosition);
        } catch (HttpResponseException e) {
            return ThrowIncorrectCredentialsException(e);
        }
    }

    @Override
    public void authenticate(String code) throws AuthenticationException, AuthorizationException {
        try {
            final KeyCardLoginResponse keyCardLoginResponse = apiClient.keyCardLogin(code);
            final CustomerEntity customerEntity = keyCardLoginResponse.getCustomer();
            persistentStorage.put(Storage.CUSTOMER_ENTITY, customerEntity);
            persistentStorage.put(Storage.CUSTOMER_ID, customerEntity.getCustomerId());
            persistentStorage.put(Storage.CUSTOMER_USER_ID, customerEntity.getAnalyticsUserId());
            persistentStorage.put(Storage.DEVICE_ID, keyCardLoginResponse.getDeviceId());
            persistentStorage.put(Storage.LOGIN_TOKEN, keyCardLoginResponse.getLoginToken());
        } catch (HttpResponseException e) {
            ThrowIncorrectCredentialsException(e);
        }
    }

    private KeyCardInitValues ThrowIncorrectCredentialsException(HttpResponseException e)
            throws AuthenticationException, AuthorizationException {
        final StatusEntity status =
                e.getResponse().getBody(UserPasswordLoginResponse.class).getStatus();

        if (status.isUserBlocked()) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                    new LocalizableKey(
                            String.format(
                                    ("%s: %s"),
                                    status.getMessage(),
                                    status.getLocalizedMessage())));
        }

        throw LoginError.INCORRECT_CREDENTIALS.exception(
                new LocalizableKey(
                        String.format(
                                ("%s: %s"), status.getMessage(), status.getLocalizedMessage())));
    }
}
