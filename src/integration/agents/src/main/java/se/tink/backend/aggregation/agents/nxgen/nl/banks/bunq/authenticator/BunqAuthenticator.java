package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BunqAuthenticator implements Authenticator {
    private final CredentialsRequest request;
    private final BunqRegistrationAuthenticator registrationAuthenticator;
    private final BunqAutoAuthenticator autoAuthenticator;

    public BunqAuthenticator(CredentialsRequest request, BunqRegistrationAuthenticator registrationAuthenticator,
            BunqAutoAuthenticator authenticationAuthenticator) {
        this.request = Preconditions.checkNotNull(request);
        this.registrationAuthenticator = Preconditions.checkNotNull(registrationAuthenticator);
        this.autoAuthenticator = Preconditions.checkNotNull(authenticationAuthenticator);
    }

    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {
        if (request.isCreate() || request.isUpdate()) {
            registration(credentials);
        } else {
            authentication(credentials);
        }
    }

    private void registration(Credentials credentials) throws AuthenticationException, AuthorizationException {
        if (!request.isManual()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        registrationAuthenticator.authenticate(credentials);
    }

    private void authentication(Credentials credentials) throws AuthenticationException, AuthorizationException {
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
