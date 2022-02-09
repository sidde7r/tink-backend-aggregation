package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.passwordandapp;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

/**
 * This Authentication Controller should handle the case, when multifactor authentication with
 * password and then the external app is needed. An external app an application means that it
 * doesn't need to be installed on the same device and the polling of status is performed towards
 * bank backend.
 *
 * @param <T>
 */
public class PasswordExternalAppAuthenticationController<T> implements TypedAuthenticator {

    private static final int DEFAULT_MAX_ATTEMPTS = 90;
    private static final long SLEEP_SECONDS = TimeUnit.SECONDS.toSeconds(2);
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final PasswordAuthenticator passwordAuthentication;
    private final ExternalAppAuthenticator<T> appAuthenticator;
    private final int maxPollAttempts;
    private ExternalThirdPartyAppResponse<T> response;

    public PasswordExternalAppAuthenticationController(
            PasswordAuthenticator passwordAuthenticator,
            ExternalAppAuthenticator<T> appAuthenticator,
            SupplementalInformationHelper supplementalInformationHelper,
            int maxPollAttempts) {
        Preconditions.checkArgument(maxPollAttempts > 0);
        this.passwordAuthentication = passwordAuthenticator;
        this.appAuthenticator = appAuthenticator;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.maxPollAttempts = maxPollAttempts;
    }

    public PasswordExternalAppAuthenticationController(
            PasswordAuthenticator passwordAuthenticator,
            ExternalAppAuthenticator<T> appAuthenticator,
            SupplementalInformationHelper supplementalInformationHelper) {
        this(
                passwordAuthenticator,
                appAuthenticator,
                supplementalInformationHelper,
                DEFAULT_MAX_ATTEMPTS);
    }

    @Override
    public CredentialsTypes getType() {
        // TODO: Change to a multifactor type when supported
        return CredentialsTypes.PASSWORD;
    }

    private ThirdPartyAppException decorateException(
            ThirdPartyAppStatus status, ThirdPartyAppError error) {
        Optional<LocalizableKey> authenticatorMessage =
                appAuthenticator.getUserErrorMessageFor(status);
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
            case AUTHENTICATION_ERROR:
                throw decorateException(status, ThirdPartyAppError.AUTHENTICATION_ERROR);
            default:
                throw new IllegalStateException(
                        String.format("Unknown status: %s", status.toString()));
        }
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        passwordAuthentication.authenticate(username, password);

        ExternalThirdPartyAppResponse<T> response = appAuthenticator.init();

        this.response = poll(response);
    }

    public ExternalThirdPartyAppResponse<T> getResponse() {
        return response;
    }

    private ExternalThirdPartyAppResponse<T> poll(ExternalThirdPartyAppResponse<T> response)
            throws AuthenticationException, AuthorizationException {

        for (int i = 0; i < maxPollAttempts; i++) {
            response = appAuthenticator.collect(response.getReference());
            if (handleStatus(response.getStatus())) {
                return response;
            }

            Uninterruptibles.sleepUninterruptibly(SLEEP_SECONDS, TimeUnit.SECONDS);
        }

        // Treat poll exhaustion as a timeout.
        throw decorateException(ThirdPartyAppStatus.TIMED_OUT, ThirdPartyAppError.TIMED_OUT);
    }
}
