package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.rpc.ConsentScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

@RequiredArgsConstructor
public class ConsentDecoupledStep implements AuthenticationStep {

    private final ConsentProcessor consentProcessor;
    private final ConsentManager consentManager;
    private final StrongAuthenticationState strongAuthenticationState;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        consentProcessor.processConsent(
                (ConsentScaResponse)
                        consentManager.createAllPsd2Consent(
                                strongAuthenticationState.getState(),
                                AccessType.ALL_ACCOUNTS_WITH_OWNER_NAME));
        return AuthenticationStepResponse.executeNextStep();
    }
}
