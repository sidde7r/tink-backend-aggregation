package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.controller;

import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.IngCardReaderAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.LoadedAuthenticationRequest;

public final class FinalStep {

    private final IngCardReaderAuthenticator authenticator;

    private static final String SIGN_ID = "signId";

    private static Logger logger = LoggerFactory.getLogger(FinalStep.class);

    FinalStep(final IngCardReaderAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    public AuthenticationResponse respond(LoadedAuthenticationRequest request)
            throws AuthenticationException {
        logger.info("ING step3: {}", request.getUserInputs());

        authenticator.confirmEnroll(
                request.getCredentials().getField(Field.Key.USERNAME),
                extractSignCodeInput(request),
                request.getCredentials().getSensitivePayload(SIGN_ID));
        authenticator.authenticate(request.getCredentials().getField(Field.Key.USERNAME));
        return new AuthenticationResponse(
                AuthenticationStepConstants.STEP_FINALIZE, Collections.emptyList());
    }

    private static String extractSignCodeInput(final AuthenticationRequest request) {
        // MIYAG-490: In production it has been observed that, in general, the list of user inputs
        // only consists of the 'signcodeinput', which is what we are interested in:
        // {signcodeinput=12345678}
        // In rare cases however, the 'signcodedescription' is also included for some reason:
        // {signcodedescription=4321 8765 09, signcodeinput=12345678}

        return request.getUserInputs().stream()
                .filter(input -> !input.contains(" "))
                .findAny()
                .orElseThrow(IllegalStateException::new);
    }
}
