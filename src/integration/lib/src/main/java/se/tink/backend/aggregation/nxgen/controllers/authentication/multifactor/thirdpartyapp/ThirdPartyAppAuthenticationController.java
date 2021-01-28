package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.i18n.LocalizableKey;

public class ThirdPartyAppAuthenticationController<T> implements TypedAuthenticator {

    protected final ThirdPartyAppAuthenticator<T> authenticator;
    protected final SupplementalInformationHelper supplementalInformationHelper;
    private final int maxPollAttempts;
    protected ThirdPartyAppResponse<T> response;

    private static final int DEFAULT_MAX_ATTEMPTS = 90;
    private static final long SLEEP_SECONDS = TimeUnit.SECONDS.toSeconds(2);

    public ThirdPartyAppAuthenticationController(
            ThirdPartyAppAuthenticator<T> authenticator,
            SupplementalInformationHelper supplementalInformationHelper) {
        this(authenticator, supplementalInformationHelper, DEFAULT_MAX_ATTEMPTS);
    }

    public ThirdPartyAppAuthenticationController(
            ThirdPartyAppAuthenticator<T> authenticator,
            SupplementalInformationHelper supplementalInformationHelper,
            int maxPollAttempts) {
        Preconditions.checkArgument(maxPollAttempts > 0);
        this.authenticator = authenticator;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.maxPollAttempts = maxPollAttempts;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        ThirdPartyAppResponse<T> response = authenticator.init();

        openThirdPartyApp();

        handleStatus(response.getStatus());

        this.response = poll(response);
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.THIRD_PARTY_APP;
    }

    private void openThirdPartyApp() {
        ThirdPartyAppAuthenticationPayload payload = authenticator.getAppPayload();
        Preconditions.checkNotNull(payload);

        supplementalInformationHelper.openThirdPartyApp(payload);
    }

    private ThirdPartyAppException decorateException(
            ThirdPartyAppStatus status, ThirdPartyAppError error) {
        Optional<LocalizableKey> authenticatorMessage =
                authenticator.getUserErrorMessageFor(status);
        return error.exception(authenticatorMessage.orElse(error.userMessage()));
    }

    protected boolean handleStatus(ThirdPartyAppStatus status)
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
            case AUTHENTICATION_ERROR:
                throw decorateException(status, ThirdPartyAppError.AUTHENTICATION_ERROR);
            default:
                throw new IllegalStateException(
                        String.format("Unknown status: %s", status.toString()));
        }
    }

    public ThirdPartyAppResponse<T> getResponse() {
        return response;
    }

    protected ThirdPartyAppResponse<T> poll(ThirdPartyAppResponse<T> response)
            throws AuthenticationException, AuthorizationException {

        for (int i = 0; i < maxPollAttempts; i++) {
            response = authenticator.collect(response.getReference());
            if (handleStatus(response.getStatus())) {
                return response;
            }

            Uninterruptibles.sleepUninterruptibly(SLEEP_SECONDS, TimeUnit.SECONDS);
        }

        // Treat poll exhaustion as a timeout.
        throw decorateException(ThirdPartyAppStatus.TIMED_OUT, ThirdPartyAppError.TIMED_OUT);
    }
}
