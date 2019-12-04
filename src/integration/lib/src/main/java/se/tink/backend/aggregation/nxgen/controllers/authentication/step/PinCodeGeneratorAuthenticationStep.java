package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import java.util.Optional;
import java.util.Random;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;

public class PinCodeGeneratorAuthenticationStep implements AuthenticationStep {

    private static final int DEFAULT_LENGTH = 4;
    private final int length;
    private final SingleFieldCallbackProcessor callbackProcessor;

    public PinCodeGeneratorAuthenticationStep(
            final SingleFieldCallbackProcessor callbackProcessor) {
        length = DEFAULT_LENGTH;
        this.callbackProcessor = callbackProcessor;
    }

    public PinCodeGeneratorAuthenticationStep(
            final SingleFieldCallbackProcessor callbackProcessor, int length) {
        this.length = length;
        this.callbackProcessor = callbackProcessor;
    }

    @Override
    public Optional<SupplementInformationRequester> execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        Random random = new Random();
        StringBuilder pin = new StringBuilder();
        for (int i = 1; i <= length; i++) {
            pin.append(random.nextInt(10));
        }
        callbackProcessor.process(pin.toString());
        return Optional.empty();
    }
}
