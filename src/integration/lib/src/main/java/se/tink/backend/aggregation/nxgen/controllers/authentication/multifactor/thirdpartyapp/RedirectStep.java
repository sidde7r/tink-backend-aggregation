package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationProgressiveController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.i18n.LocalizableKey;

final class RedirectStep<T> implements AuthenticationStep {

    private final OAuth2AuthenticationProgressiveController authenticator;
    private final SupplementalInformationHelper supplementalInformationHelper;

    RedirectStep(
            final OAuth2AuthenticationProgressiveController authenticator,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.authenticator = authenticator;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public AuthenticationResponse respond(final AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        ThirdPartyAppResponse<String> response = authenticator.init();

        handleStatus(response.getStatus());

        final Map<String, String> callbackData =
                supplementalInformationHelper
                        .waitForSupplementalInformation(
                                authenticator.getStrongAuthenticationStateSupplementalKey(),
                                authenticator.getWaitForMinutes(),
                                TimeUnit.MINUTES)
                        .orElseThrow(
                                LoginError.INCORRECT_CREDENTIALS
                                        ::exception); // todo: change this exception

        response = authenticator.collect(response.getReference(), callbackData);

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
