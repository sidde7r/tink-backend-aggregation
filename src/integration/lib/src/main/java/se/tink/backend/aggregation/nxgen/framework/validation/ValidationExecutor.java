package se.tink.backend.aggregation.nxgen.framework.validation;

import javax.annotation.Nonnull;

public interface ValidationExecutor {
    void execute(@Nonnull ValidationResult result);
}
