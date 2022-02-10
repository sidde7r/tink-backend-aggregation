package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.data.GetLoginDetailsStatus;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.helpers.AktiaAccessTokenRetriever;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.helpers.AktiaLoginDetailsFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@RequiredArgsConstructor
public class LoginStep implements AuthenticationStep {

    final AktiaAccessTokenRetriever accessTokenRetriever;
    final AktiaLoginDetailsFetcher loginDetailsFetcher;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        accessTokenRetriever.getFromRequestAndStore(request);

        final GetLoginDetailsStatus loginDetailsStatus = loginDetailsFetcher.getLoginDetails();

        if (loginDetailsStatus == GetLoginDetailsStatus.ERROR_IN_RESPONSE) {
            throw new IllegalArgumentException("Error in response from server.");
        } else if (loginDetailsStatus == GetLoginDetailsStatus.PASSWORD_CHANGE_REQUIRED) {
            throw SessionError.SESSION_EXPIRED.exception(
                    new LocalizableKey("Password change is required."));
        } else if (loginDetailsStatus == GetLoginDetailsStatus.ACCOUNT_LOCKED) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception();
        } else if (loginDetailsStatus == GetLoginDetailsStatus.MUST_ACCEPT_TERMS) {
            throw SessionError.SESSION_EXPIRED.exception(
                    new LocalizableKey("User must accept terms in other channel."));
        } else if (loginDetailsStatus == GetLoginDetailsStatus.OTP_REQUIRED) {
            return AuthenticationStepResponse.executeNextStep();
        } else {
            return AuthenticationStepResponse.authenticationSucceeded();
        }
    }

    @Override
    public String getIdentifier() {
        return "login_step";
    }
}
