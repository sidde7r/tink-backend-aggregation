package se.tink.backend.aggregation.nxgen.controllers.authentication.automatic;

import com.google.common.base.Preconditions;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AutoAuthenticationController implements TypedAuthenticator {

    private static final Logger log = LoggerFactory.getLogger(AutoAuthenticationController.class);
    private final CredentialsRequest request;
    private final SystemUpdater systemUpdater;
    private final TypedAuthenticator manualAuthenticator;
    private final AutoAuthenticator autoAuthenticator;

    public AutoAuthenticationController(
            CredentialsRequest request,
            SystemUpdater systemUpdater,
            TypedAuthenticator manualAuthenticator,
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
            if (shouldDoManualAuthentication(request)) {
                manual(credentials);
            } else {
                Preconditions.checkState(
                        isNotCredentialsType(request, CredentialsRequestType.CREATE));
                auto(credentials);
            }
        } finally {
            systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, false);
        }
    }

    private boolean shouldDoManualAuthentication(final CredentialsRequest request) {
        boolean shouldAutoAuthBeForced = shouldAutoAuthBeForced();
        boolean doesCredentialsTypeMatchAuthenticator =
                doesCredentialsTypeMatchAuthenticator(request.getCredentials());
        boolean isUpdate = request.isUpdate();
        boolean isNotTransfer = isNotTransferType(request);
        boolean shouldManualAuthBeForced = request.shouldManualAuthBeForced();

        log.info(
                "[AutoAuthenticationController] shouldAutoAuthBeForced: {}, doesCredentialsTypeMatchAuthenticator: {}, isUpdate: {}, isNotTransferType: {}, shouldManualAuthBeForced: {}",
                shouldAutoAuthBeForced,
                doesCredentialsTypeMatchAuthenticator,
                isUpdate,
                isNotTransfer,
                shouldManualAuthBeForced);

        return !shouldAutoAuthBeForced
                        && (doesCredentialsTypeMatchAuthenticator || (isUpdate && isNotTransfer))
                || shouldManualAuthBeForced;
    }

    private boolean isNotTransferType(CredentialsRequest request) {
        return !Objects.equals(request.getType(), CredentialsRequestType.TRANSFER);
    }

    private boolean isNotCredentialsType(CredentialsRequest request, CredentialsRequestType type) {
        return !Objects.equals(request.getType(), type);
    }

    private boolean doesCredentialsTypeMatchAuthenticator(Credentials credentials) {
        return Objects.equals(manualAuthenticator.getType(), credentials.getType());
    }

    // TODO: Remove this when there is support for new MultiFactor credential types.
    private boolean shouldAutoAuthBeForced() {
        return Objects.equals(manualAuthenticator.getType(), CredentialsTypes.PASSWORD)
                && !request.isUpdate()
                && !request.isCreate();
    }

    private void manual(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        if (!request.getUserAvailability().isUserAvailableForInteraction()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        if (!doesCredentialsTypeMatchAuthenticator(credentials)) {
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
            if (!request.getUserAvailability().isUserAvailableForInteraction()) {
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
