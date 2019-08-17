package se.tink.backend.aggregation.nxgen.controllers.authentication.automatic;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.Objects;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveTypedAuthenticator;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.CredentialsRequestType;

public class AutoAuthenticationProgressiveController implements ProgressiveAuthenticator {
    private final CredentialsRequest request;
    private final SystemUpdater systemUpdater;
    private final ProgressiveTypedAuthenticator manualAuthenticator;
    private final AutoAuthenticator autoAuthenticator;

    public AutoAuthenticationProgressiveController(
            CredentialsRequest request,
            SystemUpdater systemUpdater,
            ProgressiveTypedAuthenticator manualAuthenticator,
            AutoAuthenticator autoAuthenticator) {
        this.request = Preconditions.checkNotNull(request);
        this.systemUpdater = Preconditions.checkNotNull(systemUpdater);
        this.manualAuthenticator = Preconditions.checkNotNull(manualAuthenticator);
        this.autoAuthenticator = Preconditions.checkNotNull(autoAuthenticator);
    }

    @Override
    public Iterable<? extends AuthenticationStep> authenticationSteps(final Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        try {
            if (!forceAutoAuthentication()
                            && (Objects.equals(manualAuthenticator.getType(), credentials.getType())
                                    || (request.isUpdate()
                                            && !Objects.equals(
                                                    request.getType(),
                                                    CredentialsRequestType.TRANSFER)))
                    || request.getCredentials().forceManualAuthentication()) {
                return manualProgressive(credentials);
            } else {
                Preconditions.checkState(
                        !Objects.equals(request.getType(), CredentialsRequestType.CREATE));
                return auto(credentials);
            }
        } finally {
            // TODO auth: move it up layer
            systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, false);
        }
    }

    // TODO: Remove this when there is support for new MultiFactor credential types.
    private boolean forceAutoAuthentication() {
        return Objects.equals(manualAuthenticator.getType(), CredentialsTypes.PASSWORD)
                && !request.isUpdate()
                && !request.isCreate();
    }

    private Iterable<? extends AuthenticationStep> manualProgressive(final Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        if (!request.isManual()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        if (!Objects.equals(manualAuthenticator.getType(), credentials.getType())) {
            credentials.setType(manualAuthenticator.getType());
        }
        try {
            return manualAuthenticator.authenticationSteps(credentials);
        } finally {
            credentials.setType(CredentialsTypes.PASSWORD);
        }
    }

    private Iterable<? extends AuthenticationStep> auto(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        try {
            autoAuthenticator.autoAuthenticate();
            return Collections.singletonList(
                    request -> new AuthenticationResponse(Collections.emptyList()));
        } catch (SessionException autoException) {
            if (!request.isManual()) {
                credentials.setType(manualAuthenticator.getType());

                throw autoException;
            }

            try {
                manualProgressive(credentials);
                return Collections.singletonList(
                        request -> new AuthenticationResponse(Collections.emptyList()));
            } catch (AuthenticationException | AuthorizationException manualException) {
                credentials.setType(manualAuthenticator.getType());

                throw manualException;
            }
        }
    }
}
