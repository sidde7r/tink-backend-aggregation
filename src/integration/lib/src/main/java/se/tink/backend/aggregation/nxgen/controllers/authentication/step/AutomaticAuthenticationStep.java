package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;

public class AutomaticAuthenticationStep implements AuthenticationStep {

    private ProcessCallback processCallback;
    private String stepId;

    public interface ProcessCallback {
        void process() throws AuthenticationException;
    }

    public AutomaticAuthenticationStep(ProcessCallback processCallback, String stepId) {
        this.processCallback = processCallback;
        this.stepId = stepId;
    }

    @Override
    public Optional<SupplementInformationRequester> execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        processCallback.process();
        return Optional.empty();
    }

    @Override
    public String getIdentifier() {
        return stepId;
    }
}
