package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;

public class AutomaticAuthenticationStep implements AuthenticationStep {

    private CallbackProcessorEmpty callbackProcessor;
    private String stepId;

    public AutomaticAuthenticationStep(CallbackProcessorEmpty callbackProcessor, String stepId) {
        this.callbackProcessor = callbackProcessor;
        this.stepId = stepId;
    }

    @Override
    public Optional<SupplementInformationRequester> execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        callbackProcessor.process();
        return Optional.empty();
    }

    @Override
    public String getIdentifier() {
        return stepId;
    }
}
