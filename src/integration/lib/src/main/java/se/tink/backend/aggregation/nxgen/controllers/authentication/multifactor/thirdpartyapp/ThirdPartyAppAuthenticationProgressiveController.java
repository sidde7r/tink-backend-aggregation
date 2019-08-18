package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveTypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.libraries.i18n.LocalizableKey;

public class ThirdPartyAppAuthenticationProgressiveController<T>
        implements ProgressiveTypedAuthenticator {

    private final ThirdPartyAppAuthenticator<T> authenticator;
    private final int maxPollAttempts;

    private static final int DEFAULT_MAX_ATTEMPTS = 90;
    private static final long SLEEP_SECONDS = TimeUnit.SECONDS.toSeconds(2);

    public ThirdPartyAppAuthenticationProgressiveController(
            ThirdPartyAppAuthenticator<T> authenticator) {
        this(authenticator, DEFAULT_MAX_ATTEMPTS);
    }

    public ThirdPartyAppAuthenticationProgressiveController(
            ThirdPartyAppAuthenticator<T> authenticator, int maxPollAttempts) {
        Preconditions.checkArgument(maxPollAttempts > 0);
        this.authenticator = authenticator;
        this.maxPollAttempts = maxPollAttempts;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.THIRD_PARTY_APP;
    }

    private ThirdPartyAppException decorateException(
            ThirdPartyAppStatus status, ThirdPartyAppError error) {
        Optional<LocalizableKey> authenticatorMessage =
                authenticator.getUserErrorMessageFor(status);
        return error.exception(authenticatorMessage.orElse(error.userMessage()));
    }

    private boolean handleStatus(ThirdPartyAppStatus status)
            throws AuthenticationException, AuthorizationException {
        if (status == null) {
            throw new IllegalStateException(String.format("Status missing"));
        }
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
                throw new IllegalStateException(
                        String.format("Unknown status: %s", status.toString()));
        }
    }

    @Override
    public Iterable<? extends AuthenticationStep> authenticationSteps(
            final Credentials credentials) {
        return Arrays.asList(
                request -> {
                    ThirdPartyAppAuthenticationPayload payload = authenticator.getAppPayload();
                    Preconditions.checkNotNull(payload);

                    return AuthenticationResponse.openThirdPartyApp(payload);
                },
                request -> {
                    ThirdPartyAppResponse<T> response = authenticator.init();

                    handleStatus(response.getStatus());

                    poll(response);

                    return new AuthenticationResponse(Collections.emptyList());
                });
    }

    private void poll(ThirdPartyAppResponse<T> response)
            throws AuthenticationException, AuthorizationException {

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
