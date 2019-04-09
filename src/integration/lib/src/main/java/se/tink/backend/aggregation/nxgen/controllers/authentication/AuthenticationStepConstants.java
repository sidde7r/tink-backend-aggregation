package se.tink.backend.aggregation.nxgen.controllers.authentication;

/**
 * Define init and finalize steps here. To define intermediate steps, define it locally in the
 * agent.
 */
public final class AuthenticationStepConstants {

    private AuthenticationStepConstants() {
        throw new AssertionError();
    }

    public static final String STEP_INIT = "init";
    public static final String STEP_FINALIZE = "finalize";
}
