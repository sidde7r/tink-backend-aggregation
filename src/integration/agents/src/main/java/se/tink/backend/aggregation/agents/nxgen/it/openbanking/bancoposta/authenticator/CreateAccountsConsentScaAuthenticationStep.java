package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.rpc.ConsentScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

public class CreateAccountsConsentScaAuthenticationStep implements AuthenticationStep {

    private final ConsentManager consentManager;
    private final StrongAuthenticationState strongAuthenticationState;
    private final CbiUserState userState;

    CreateAccountsConsentScaAuthenticationStep(
            final ConsentManager consentManager,
            final StrongAuthenticationState strongAuthenticationState,
            final CbiUserState userState) {
        this.consentManager = consentManager;
        this.strongAuthenticationState = strongAuthenticationState;
        this.userState = userState;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        ConsentScaResponse consentResponse =
                (ConsentScaResponse)
                        consentManager.createAccountConsent(strongAuthenticationState.getState());

        userState.saveChosenAuthenticationMethod(
                consentResponse.getScaMethod().getAuthenticationMethodId());

        return AuthenticationStepResponse.executeNextStep();
    }
}
