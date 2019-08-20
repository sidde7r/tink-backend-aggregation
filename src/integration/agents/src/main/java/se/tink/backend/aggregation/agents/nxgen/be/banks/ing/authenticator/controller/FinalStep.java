package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.controller;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.IngCardReaderAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;

public final class FinalStep implements AuthenticationStep {

    private final IngCardReaderAuthenticator authenticator;

    private static final String SIGN_ID = "signId";

    private static Logger logger = LoggerFactory.getLogger(FinalStep.class);

    FinalStep(final IngCardReaderAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public AuthenticationResponse respond(final AuthenticationRequest request)
            throws AuthenticationException {
        logger.info("ING FinalStep: {}", request.getUserInputs());

        authenticator.confirmEnroll(
                request.getCredentials().getField(Field.Key.USERNAME),
                extractSignCodeInput(request.getUserInputs()),
                request.getCredentials().getSensitivePayload(SIGN_ID));
        authenticator.authenticate(request.getCredentials().getField(Field.Key.USERNAME));
        return AuthenticationResponse.fromSupplementalFields(Collections.emptyList());
    }

    private static String extractSignCodeInput(final ImmutableList<String> userInputs) {
        // MIYAG-490: In production it has been observed that, in general, the list of user inputs
        // only consists of the 'signcodeinput', which is what we are interested in:
        // {signcodeinput=12345678}
        // In rare cases however, the 'signcodedescription' is also included for some reason:
        // {signcodedescription=4321 8765 09, signcodeinput=12345678}

        return userInputs.stream()
                .filter(input -> !input.contains(" "))
                .findAny()
                .orElseThrow(IllegalStateException::new);
    }
}
