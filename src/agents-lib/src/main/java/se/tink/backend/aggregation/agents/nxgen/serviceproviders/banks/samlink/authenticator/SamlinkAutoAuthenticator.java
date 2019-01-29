package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator;

import com.google.common.base.Strings;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.agents.rpc.Credentials;

public class SamlinkAutoAuthenticator extends SamlinkAuthenticatorBase implements AutoAuthenticator {

    private static final AggregationLogger LOGGER = new AggregationLogger(SamlinkAutoAuthenticator.class);

    private final SamlinkPersistentStorage persistentStorage;

    public SamlinkAutoAuthenticator(SamlinkApiClient apiClient, SamlinkPersistentStorage persistentStorage,
            Credentials credentials) {
        super(LOGGER, credentials, apiClient);
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);
        String deviceId = persistentStorage.getDeviceId();
        String deviceToken = persistentStorage.getDeviceToken();

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password) || Strings.isNullOrEmpty(deviceId) ||
                Strings.isNullOrEmpty(deviceToken)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        LoginResponse loginResponse;
        try {
            loginResponse = apiClient.login(username, password, deviceId, deviceToken);
            deviceToken = loginResponse.getDeviceToken();
        } catch (HttpResponseException e) {
            handleAndThrowAutoAuthenticateError(e);
        }

        // Update device token
        persistentStorage.putDeviceToken(deviceToken);
    }
}
