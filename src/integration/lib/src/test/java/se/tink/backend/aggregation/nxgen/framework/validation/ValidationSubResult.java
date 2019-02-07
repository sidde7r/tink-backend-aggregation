package se.tink.backend.aggregation.nxgen.framework.validation;

/** The result of validating one criterion. */
public final class ValidationSubResult {

    private final boolean passed;
    private final String message;

    public ValidationSubResult(final boolean passed, final String message) {
        this.passed = passed;
        this.message = message;
    }

    public boolean passed() {
        return passed;
    }

    public String getMessage() {
        return message;
    }
}
