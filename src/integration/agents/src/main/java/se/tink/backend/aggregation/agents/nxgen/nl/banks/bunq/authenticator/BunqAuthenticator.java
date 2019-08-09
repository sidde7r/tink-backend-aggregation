package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator;

import com.google.common.base.Preconditions;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BunqAuthenticator implements Authenticator {
    private final CredentialsRequest request;
    private final BunqRegistrationAuthenticator registrationAuthenticator;
    private final BunqAutoAuthenticator autoAuthenticator;

    public BunqAuthenticator(
            CredentialsRequest request,
            BunqRegistrationAuthenticator registrationAuthenticator,
            BunqAutoAuthenticator authenticationAuthenticator) {
        this.request = Preconditions.checkNotNull(request);
        this.registrationAuthenticator = Preconditions.checkNotNull(registrationAuthenticator);
        this.autoAuthenticator = Preconditions.checkNotNull(authenticationAuthenticator);
    }

    public static void updateClientAuthToken(
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            TemporaryStorage temporaryStorage) {
        TokenEntity newClientAuthToken =
                persistentStorage
                        .get(
                                BunqBaseConstants.StorageKeys.USER_CLIENT_AUTH_TOKEN,
                                TokenEntity.class)
                        .orElseThrow(
                                () -> new IllegalStateException("No client auth token found."));
        sessionStorage.put(BunqBaseConstants.StorageKeys.CLIENT_AUTH_TOKEN, newClientAuthToken);
        temporaryStorage.put(
                newClientAuthToken.getToken(),
                persistentStorage.get(
                        BunqBaseConstants.StorageKeys.USER_DEVICE_RSA_SIGNING_KEY_PAIR));
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        if (request.isCreate() || request.isUpdate()) {
            registration(credentials);
        } else {
            authentication(credentials);
        }
    }

    private void registration(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        if (!request.isManual()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        registrationAuthenticator.authenticate(credentials);
    }

    private void authentication(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        try {
            autoAuthenticator.autoAuthenticate();
        } catch (SessionException autoException) {
            if (!request.isManual()) {
                throw autoException;
            }

            registration(credentials);
        }
    }
}
