package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator;

import com.google.common.base.Strings;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.encap.SpankkiEncapClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.encap.SpankkiEncapConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.utils.SpankkiAuthUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SpankkiAutoAuthenticator implements AutoAuthenticator {
    private final SpankkiApiClient apiClient;
    private final Credentials credentials;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    public SpankkiAutoAuthenticator(
            SpankkiApiClient apiClient,
            Credentials credentials,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.credentials = credentials;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        final String username = credentials.getField(Field.Key.USERNAME);
        final String password = credentials.getField(Field.Key.PASSWORD);
        final String deviceId = persistentStorage.get(Storage.DEVICE_ID);
        final String loginToken = persistentStorage.get(Storage.LOGIN_TOKEN);

        if (Strings.isNullOrEmpty(username)
                || Strings.isNullOrEmpty(password)
                || Strings.isNullOrEmpty(deviceId)
                || Strings.isNullOrEmpty(loginToken)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        final SpankkiEncapClient encapClient =
                new SpankkiEncapClient(
                        new SpankkiEncapConfiguration(), persistentStorage, apiClient, username);
        try {
            final SpankkiAuthUtils authUtils = new SpankkiAuthUtils(apiClient);
            authUtils.solveChallenge();
            encapClient.authenticateDevice();
        } catch (BankServiceException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        } finally {
            // Always save the device
            encapClient.saveDevice();
        }
    }
}
