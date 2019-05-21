package se.tink.backend.aggregation.nxgen.controllers.authentication;

import java.util.List;

/**
 * In progressive authentication, carry request information such as step, userInputs and credential.
 */
public final class AuthenticationRequestImpl implements AuthenticationRequest {
    private final String step;
    private final List<String> userInputs;

    public AuthenticationRequestImpl(final String step, final List<String> userInputs) {
        this.step = step;
        this.userInputs = userInputs;
    }

    @Override
    public String getStep() {
        return step;
    }

    @Override
    public List<String> getUserInputs() {
        return userInputs;
    }
}
