package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import java.security.SecureRandom;
import java.util.Random;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

public class PinCodeGeneratorAuthenticationStep extends AbstractAuthenticationStep {

    private static final int DEFAULT_LENGTH = 4;
    private static final Random RAND = new SecureRandom();
    private final int length;
    private final CallbackProcessorSingleData callbackProcessor;

    public PinCodeGeneratorAuthenticationStep(final CallbackProcessorSingleData callbackProcessor) {
        length = DEFAULT_LENGTH;
        this.callbackProcessor = callbackProcessor;
    }

    public PinCodeGeneratorAuthenticationStep(
            final CallbackProcessorSingleData callbackProcessor, int length) {
        this.length = length;
        this.callbackProcessor = callbackProcessor;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        StringBuilder pin = new StringBuilder();
        for (int i = 1; i <= length; i++) {
            pin.append(RAND.nextInt(10));
        }
        return callbackProcessor.process(pin.toString());
    }
}
