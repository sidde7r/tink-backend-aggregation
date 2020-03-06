package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.steps;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepResponse;

public abstract class StepTestBase {

    AuthenticationStepResponse executeStepAndGetResponse(
            AuthenticationStep authenticationStep, AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        return authenticationStep.execute(request);
    }

    AuthenticationStepResponse executeStepAndGetResponse(AuthenticationStep authenticationStep) {
        try {
            return authenticationStep.execute(null);
        } catch (AuthorizationException | AuthenticationException e) {
            throw new RuntimeException(e);
        }
    }
}
