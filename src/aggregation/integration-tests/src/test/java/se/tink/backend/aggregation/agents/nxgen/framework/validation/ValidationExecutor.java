package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import javax.annotation.Nonnull;

public interface ValidationExecutor {
    void execute(@Nonnull ValidationResult result);
}
