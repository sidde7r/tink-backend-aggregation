package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepResponse;

public class AutomaticAuthenticationStep extends AbstractAuthenticationStep {

    private CallbackProcessorEmpty callbackProcessor;

    public AutomaticAuthenticationStep(
            final CallbackProcessorEmpty callbackProcessor, final String stepId) {
        super(stepId);
        this.callbackProcessor = callbackProcessor;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        return callbackProcessor.process();
    }
}
