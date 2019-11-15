package se.tink.backend.aggregation.nxgen.framework.validation;

import javax.annotation.Nonnull;

/** Stores the return value of the method call. Useful for testing. */
public final class SilentExecutor implements ValidationExecutor {
    private ValidationResult result = null;

    @Override
    public void execute(@Nonnull final ValidationResult result) {
        this.result = result;
    }

    public ValidationResult getResult() {
        return result;
    }
}
