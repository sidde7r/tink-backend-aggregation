package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.common.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ThirdPartyAppAuthenticationController<T> implements MultiFactorAuthenticator {

    private final ThirdPartyAppAuthenticator<T> authenticator;
    private final AgentContext context;
    private final int maxPollAttempts;

    private static final int DEFAULT_MAX_ATTEMPTS = 90;
    private static final long SLEEP_SECONDS = TimeUnit.SECONDS.toSeconds(2);

    public ThirdPartyAppAuthenticationController(ThirdPartyAppAuthenticator<T> authenticator, AgentContext context) {
        this(authenticator, context, DEFAULT_MAX_ATTEMPTS);
    }

    public ThirdPartyAppAuthenticationController(ThirdPartyAppAuthenticator<T> authenticator, AgentContext context,
            int maxPollAttempts) {
        Preconditions.checkArgument(maxPollAttempts > 0);
        this.authenticator = authenticator;
        this.context = context;
        this.maxPollAttempts = maxPollAttempts;
    }

    @Override
    public CredentialsTypes getType() {
        // Todo: Change to `THIRD_PARTY_APP`.
        return CredentialsTypes.MOBILE_BANKID;
    }

    private void openThirdPartyApp(Credentials credentials) {
        ThirdPartyAppAuthenticationPayload payload = authenticator.getAppPayload();
        Preconditions.checkNotNull(payload);

        credentials.setSupplementalInformation(SerializationUtils.serializeToString(payload));
        credentials.setStatus(CredentialsStatus.AWAITING_THIRD_PARTY_APP_AUTHENTICATION);
        context.requestSupplementalInformation(credentials, false);
    }

    private ThirdPartyAppException decorateException(ThirdPartyAppStatus status, ThirdPartyAppError error) {
        Optional<LocalizableKey> authenticatorMessage = authenticator.getUserErrorMessageFor(status);
        return error.exception(authenticatorMessage.orElse(error.userMessage()));
    }

    private boolean handleStatus(ThirdPartyAppStatus status) throws AuthenticationException, AuthorizationException {
        switch (status) {
        case WAITING:
            return false;
        case DONE:
            return true;
        case CANCELLED:
            throw decorateException(status, ThirdPartyAppError.CANCELLED);
        case TIMED_OUT:
            throw decorateException(status, ThirdPartyAppError.TIMED_OUT);
        case ALREADY_IN_PROGRESS:
            throw decorateException(status, ThirdPartyAppError.ALREADY_IN_PROGRESS);
        default:
            throw new IllegalStateException(String.format("Unknown status: %s", status.toString()));
        }
    }

    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {
        NotImplementedException.throwIf(!Objects.equals(credentials.getType(), getType()),
                String.format("Authentication method not implemented for CredentialsType: %s", credentials.getType()));

        openThirdPartyApp(credentials);

        ThirdPartyAppResponse<T> response = authenticator.init();

        handleStatus(response.getStatus());

        poll(response);
    }

    private void poll(ThirdPartyAppResponse<T> response) throws AuthenticationException, AuthorizationException {

        for (int i = 0; i < maxPollAttempts; i++) {
            response = authenticator.collect(response.getReference());
            if (handleStatus(response.getStatus())) {
                return;
            }

            Uninterruptibles.sleepUninterruptibly(SLEEP_SECONDS, TimeUnit.SECONDS);
        }

        // Treat poll exhaustion as a timeout.
        throw decorateException(ThirdPartyAppStatus.TIMED_OUT, ThirdPartyAppError.TIMED_OUT);
    }
}
