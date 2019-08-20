package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.libraries.i18n.LocalizableKey;

final class RedirectStep<T> implements AuthenticationStep {

    private static final long SLEEP_SECONDS = TimeUnit.SECONDS.toSeconds(2);

    private final ThirdPartyAppProgressiveAuthenticator<T> authenticator;
    private final int maxPollAttempts;

    RedirectStep(
            final ThirdPartyAppProgressiveAuthenticator<T> authenticator,
            final int maxPollAttempts) {
        this.authenticator = authenticator;
        this.maxPollAttempts = maxPollAttempts;
    }

    @Override
    public AuthenticationResponse respond(final AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        ThirdPartyAppResponse<T> response = authenticator.init();

        handleStatus(response.getStatus());

        boolean finished = false;

        for (int i = 0; i < maxPollAttempts && !finished; i++) {
            response = authenticator.collect(response.getReference());
            if (handleStatus(response.getStatus())) {
                finished = true;
            } else {
                Uninterruptibles.sleepUninterruptibly(SLEEP_SECONDS, TimeUnit.SECONDS);
            }
        }
        if (!finished) {
            // Treat poll exhaustion as a timeout.
            throw decorateException(ThirdPartyAppStatus.TIMED_OUT, ThirdPartyAppError.TIMED_OUT);
        }

        return new AuthenticationResponse(Collections.emptyList());
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

    private ThirdPartyAppException decorateException(
            ThirdPartyAppStatus status, ThirdPartyAppError error) {
        Optional<LocalizableKey> authenticatorMessage =
                authenticator.getUserErrorMessageFor(status);
        return error.exception(authenticatorMessage.orElse(error.userMessage()));
    }
}
