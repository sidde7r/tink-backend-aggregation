package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator;

import java.lang.invoke.MethodHandles;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.rpc.RegisterDeviceResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SamlinkKeyCardAuthenticator extends SamlinkAuthenticatorBase
        implements KeyCardAuthenticator {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SamlinkPersistentStorage persistentStorage;

    private LoginResponse loginResponse;

    public SamlinkKeyCardAuthenticator(
            SamlinkApiClient apiClient,
            SamlinkPersistentStorage persistentStorage,
            Credentials credentials) {
        super(logger, credentials, apiClient);
        this.persistentStorage = persistentStorage;
    }

    @Override
    public KeyCardInitValues init(String username, String password)
            throws AuthenticationException, AuthorizationException {
        credentials.setSensitivePayload(Field.Key.USERNAME, username);
        credentials.setSensitivePayload(Field.Key.PASSWORD, password);
        try {
            loginResponse = apiClient.login(username, password);

            String keyCardId = loginResponse.getSecurityKeyIndex().getCard().getId();
            String keyCardCodeIndex = loginResponse.getSecurityKeyIndex().getIndex();

            credentials.setSensitivePayload("key-card-id", keyCardId);
            credentials.setSensitivePayload("key-card-code-index", keyCardCodeIndex);
            return new KeyCardInitValues(keyCardId, keyCardCodeIndex);
        } catch (HttpResponseException e) {
            handleAndThrowInitError(e);
        }
        return null;
    }

    @Override
    public void authenticate(String code) throws AuthenticationException, AuthorizationException {
        String deviceId = UUID.randomUUID().toString();
        credentials.setSensitivePayload(Field.Key.OTP_INPUT, code);
        try {
            RegisterDeviceResponse registerDeviceResponse =
                    apiClient.registerDevice(loginResponse.getLinks(), code, deviceId);

            String deviceToken = registerDeviceResponse.getDeviceToken();
            credentials.setSensitivePayload(Field.Key.ACCESS_TOKEN, deviceToken);

            persistentStorage.putDeviceId(deviceId);
            persistentStorage.putDeviceToken(deviceToken);
        } catch (HttpResponseException e) {
            handleAndThrowAuthenticateError(e);
        }
    }
}
