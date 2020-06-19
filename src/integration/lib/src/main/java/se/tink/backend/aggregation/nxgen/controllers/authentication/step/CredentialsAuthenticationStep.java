package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

public class CredentialsAuthenticationStep implements AuthenticationStep {

    public interface CallbackProcessor {
        void process(final Credentials credentials) throws AuthenticationException;
    }

    private final CallbackProcessor processor;

    public CredentialsAuthenticationStep(CallbackProcessor processor) {
        this.processor = processor;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        processor.process(request.getCredentials());
        return AuthenticationStepResponse.executeNextStep();
    }
}
