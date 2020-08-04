package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator;

import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SamlinkAutoAuthenticator extends SamlinkAuthenticatorBase
        implements AutoAuthenticator {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SamlinkPersistentStorage persistentStorage;

    public SamlinkAutoAuthenticator(
            SamlinkApiClient apiClient,
            SamlinkPersistentStorage persistentStorage,
            Credentials credentials) {
        super(logger, credentials, apiClient);
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);
        String deviceId = persistentStorage.getDeviceId();
        String deviceToken = persistentStorage.getDeviceToken();

        credentials.setSensitivePayload(Field.Key.USERNAME, username);
        credentials.setSensitivePayload(Field.Key.PASSWORD, password);
        if (Strings.isNullOrEmpty(username)
                || Strings.isNullOrEmpty(password)
                || Strings.isNullOrEmpty(deviceId)
                || Strings.isNullOrEmpty(deviceToken)) {
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
        credentials.setSensitivePayload(Field.Key.ACCESS_TOKEN, deviceToken);
    }
}
