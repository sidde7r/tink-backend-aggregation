package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.ScaOptions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class DecisionAuthStep extends AbstractAuthenticationStep {
    private SessionStorage sessionStorage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        if (sessionStorage.get(StorageKeys.SCA_OPTION_KEY).equals(ScaOptions.CODEAPP_OPTION))
            return AuthenticationStepResponse.executeNextStep();
        else
            return AuthenticationStepResponse.executeStepWithId(
                    KeyCardAuthenticationStep.KEY_CARD_STEP_NAME);
    }
}
