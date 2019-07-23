package se.tink.backend.aggregation.nxgen.controllers.signing;

/**
 * Define init and finalize steps here. To define intermediate steps, define it locally in the
 * agent.
 */
public final class SigningStepConstants {

    private SigningStepConstants() {
        throw new AssertionError();
    }

    public static final String STEP_INIT = "init";
    public static final String STEP_SIGN = "sign";
    public static final String STEP_FINALIZE = "finalize";
}
