package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator;

import java.util.UUID;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.rpc.RegisterDeviceResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class SamlinkKeyCardAuthenticator extends SamlinkAuthenticatorBase
        implements KeyCardAuthenticator {
    private static final AggregationLogger LOGGER =
            new AggregationLogger(SamlinkKeyCardAuthenticator.class);

    private final SamlinkPersistentStorage persistentStorage;

    private LoginResponse loginResponse;

    public SamlinkKeyCardAuthenticator(
            SamlinkApiClient apiClient,
            SamlinkPersistentStorage persistentStorage,
            Credentials credentials) {
        super(LOGGER, credentials, apiClient);
        this.persistentStorage = persistentStorage;
    }

    @Override
    public KeyCardInitValues init(String username, String password)
            throws AuthenticationException, AuthorizationException {
        try {
            loginResponse = apiClient.login(username, password);

            String keyCardId = loginResponse.getSecurityKeyIndex().getCard().getId();
            String keyCardCodeIndex = loginResponse.getSecurityKeyIndex().getIndex();

            return new KeyCardInitValues(keyCardId, keyCardCodeIndex);
        } catch (HttpResponseException e) {
            handleAndThrowInitError(e);
        }
        return null;
    }

    @Override
    public void authenticate(String code) throws AuthenticationException, AuthorizationException {
        String deviceId = UUID.randomUUID().toString();
        try {
            RegisterDeviceResponse registerDeviceResponse =
                    apiClient.registerDevice(loginResponse.getLinks(), code, deviceId);

            String deviceToken = registerDeviceResponse.getDeviceToken();

            persistentStorage.putDeviceId(deviceId);
            persistentStorage.putDeviceToken(deviceToken);
        } catch (HttpResponseException e) {
            handleAndThrowAuthenticateError(e);
        }
    }
}
