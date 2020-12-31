package se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.progressive;

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
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.ProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.ProgressiveTypedAuthenticator;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.CredentialsRequestType;

public class AutoAuthenticationProgressiveController implements ProgressiveAuthenticator {

    private final Credentials credentials;
    private final CredentialsRequestType credentialsRequestType;
    private final boolean requestIsUpdate;
    private final boolean requestIsCreate;
    private final boolean requestIsManual;
    private final boolean requestIsForceAuthenticate;
    private final SystemUpdater systemUpdater;
    private final ProgressiveTypedAuthenticator manualAuthenticator;
    private final AutoAuthenticator autoAuthenticator;

    public AutoAuthenticationProgressiveController(
            CredentialsRequest request,
            SystemUpdater systemUpdater,
            ProgressiveTypedAuthenticator manualAuthenticator,
            AutoAuthenticator autoAuthenticator) {
        Preconditions.checkNotNull(request);
        this.credentials = request.getCredentials();
        this.credentialsRequestType = request.getType();
        this.requestIsUpdate = request.isUpdate();
        this.requestIsCreate = request.isCreate();
        this.requestIsManual = request.isManual();
        this.requestIsForceAuthenticate = request.isForceAuthenticate();

        this.systemUpdater = Preconditions.checkNotNull(systemUpdater);
        this.manualAuthenticator = Preconditions.checkNotNull(manualAuthenticator);
        this.autoAuthenticator = Preconditions.checkNotNull(autoAuthenticator);
    }

    @Override
    public Iterable<AuthenticationStep> authenticationSteps()
            throws AuthenticationException, AuthorizationException {
        try {
            if (shouldDoManualAuthentication(credentials)) {
                return manualProgressive(credentials);
            } else {
                Preconditions.checkState(
                        !Objects.equals(credentialsRequestType, CredentialsRequestType.CREATE));
                return auto(credentials);
            }
        } finally {
            // TODO auth: move it up layer
            systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, false);
        }
    }

    private boolean shouldDoManualAuthentication(final Credentials credentials) {
        return !forceAutoAuthentication()
                        && (Objects.equals(manualAuthenticator.getType(), credentials.getType())
                                || (requestIsUpdate
                                        && !Objects.equals(
                                                credentialsRequestType,
                                                CredentialsRequestType.TRANSFER)))
                || requestIsForceAuthenticate;
    }

    // TODO: Remove this when there is support for new MultiFactor credential types.
    private boolean forceAutoAuthentication() {
        return Objects.equals(manualAuthenticator.getType(), CredentialsTypes.PASSWORD)
                && !requestIsUpdate
                && !requestIsCreate;
    }

    private Iterable<AuthenticationStep> manualProgressive(final Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        if (!requestIsManual) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        if (!Objects.equals(manualAuthenticator.getType(), credentials.getType())) {
            credentials.setType(manualAuthenticator.getType());
        }

        return manualAuthenticator.authenticationSteps();
    }

    private Iterable<AuthenticationStep> auto(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        try {
            autoAuthenticator.autoAuthenticate();
            return Collections.singletonList(
                    request -> AuthenticationStepResponse.executeNextStep());
        } catch (SessionException autoException) {
            if (!requestIsManual) {
                credentials.setType(manualAuthenticator.getType());

                throw autoException;
            }

            try {
                return manualProgressive(credentials);
            } catch (AuthenticationException | AuthorizationException manualException) {
                credentials.setType(manualAuthenticator.getType());

                throw manualException;
            }
        }
    }
}
