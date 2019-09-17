package se.tink.backend.aggregation.nxgen.controllers.authentication.automatic;

import com.google.common.base.Preconditions;
import java.util.Objects;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.ManualOrAutoAuth;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.CredentialsRequestType;

public class AutoAuthenticationController implements TypedAuthenticator, ManualOrAutoAuth {
    private final CredentialsRequest request;
    private final SystemUpdater systemUpdater;
    private final MultiFactorAuthenticator manualAuthenticator;
    private final AutoAuthenticator autoAuthenticator;

    public AutoAuthenticationController(
            CredentialsRequest request,
            SystemUpdater systemUpdater,
            MultiFactorAuthenticator manualAuthenticator,
            AutoAuthenticator autoAuthenticator) {
        this.request = Preconditions.checkNotNull(request);
        this.systemUpdater = Preconditions.checkNotNull(systemUpdater);
        this.manualAuthenticator = Preconditions.checkNotNull(manualAuthenticator);
        this.autoAuthenticator = Preconditions.checkNotNull(autoAuthenticator);
    }

    @Override
    // TODO: Change to new MultiFactor credential type when available.
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    // TODO auth: remove the legacy authenticate and extension.
    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        try {
            if (isManualAuthentication(credentials)) {
                manual(credentials);
            } else {
                Preconditions.checkState(
                        !Objects.equals(request.getType(), CredentialsRequestType.CREATE));
                auto(credentials);
            }
        } finally {
            systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, false);
        }
    }

    private boolean shouldDoManualAuthentication(final Credentials credentials) {
        return !forceAutoAuthentication()
                        && (Objects.equals(manualAuthenticator.getType(), credentials.getType())
                                || (request.isUpdate()
                                        && !Objects.equals(
                                                request.getType(),
                                                CredentialsRequestType.TRANSFER)))
                || credentials.forceManualAuthentication();
    }

    @Override
    public boolean isManualAuthentication(Credentials credentials) {
        return shouldDoManualAuthentication(credentials);
    }

    // TODO: Remove this when there is support for new MultiFactor credential types.
    private boolean forceAutoAuthentication() {
        return Objects.equals(manualAuthenticator.getType(), CredentialsTypes.PASSWORD)
                && !request.isUpdate()
                && !request.isCreate();
    }

    private void manual(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        if (!request.isManual()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        if (!Objects.equals(manualAuthenticator.getType(), credentials.getType())) {
            credentials.setType(manualAuthenticator.getType());
        }

        manualAuthenticator.authenticate(credentials);
        credentials.setType(CredentialsTypes.PASSWORD);
    }

    private void auto(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        try {
            autoAuthenticator.autoAuthenticate();
        } catch (SessionException autoException) {
            if (!request.isManual()) {
                credentials.setType(manualAuthenticator.getType());

                throw autoException;
            }

            try {
                manual(credentials);
            } catch (AuthenticationException | AuthorizationException manualException) {
                credentials.setType(manualAuthenticator.getType());

                throw manualException;
            }
        }
    }
}
