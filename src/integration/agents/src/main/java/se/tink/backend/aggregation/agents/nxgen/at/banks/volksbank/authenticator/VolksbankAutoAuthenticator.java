package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class VolksbankAutoAuthenticator implements AutoAuthenticator {

    private final VolksbankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;

    public VolksbankAutoAuthenticator(
            VolksbankApiClient apiClient,
            PersistentStorage persistentStorage,
            Credentials credentials) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
    }

    @Override
    public void autoAuthenticate() throws SessionException {

        String userId = credentials.getField(VolksbankConstants.CREDENTIAL_USERNUMBER);
        String userName = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);
        String secret = persistentStorage.get(VolksbankConstants.Storage.SECRET);
        String generateId = persistentStorage.get(VolksbankConstants.Storage.GENERATE_ID);

        if (secret == null || generateId == null) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        apiClient.getLogin();
        apiClient.postExtensionsOtpLogin(userId, generateId, secret);
        apiClient.postExtensions();
        apiClient.getLoginKeepSession();
        apiClient.postLoginOtp(userId, generateId, secret);
        apiClient.getLoginUpgradeKeepSession();
        apiClient.postLoginUserNamePasswordWithGid(generateId, userId, userName, password);
        apiClient.getGenerateBinding();
        apiClient.postGenerateBindingSkipAction();
        apiClient.getMain();
    }
}
