package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.helpers.AktiaAccessTokenRetriever;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

@RequiredArgsConstructor
public class CheckIfAccessTokenIsValidStep implements AuthenticationStep {

    private final AktiaAccessTokenRetriever accessTokenRetriever;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        final AccessTokenStatus accessTokenStatus = accessTokenRetriever.getStatusFromStorage();

        if (accessTokenStatus == AccessTokenStatus.VALID) {
            return AuthenticationStepResponse.authenticationSucceeded();
        } else {
            return AuthenticationStepResponse.executeNextStep();
        }
    }

    @Override
    public String getIdentifier() {
        return "check_if_access_token_is_valid_step";
    }
}
