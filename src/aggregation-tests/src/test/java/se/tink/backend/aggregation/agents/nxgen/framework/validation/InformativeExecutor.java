package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InformativeExecutor implements ValidationExecutor {
    private static final Logger logger = LoggerFactory.getLogger(InformativeExecutor.class);

    @Override
    public void execute(final ValidationResult result) {
        final StringBuilder results = new StringBuilder();
        for (final Map.Entry<String, ValidationSubResult> entry : result.getSubResults().entrySet()) {
            final String prefix = entry.getValue().passed() ? "[PASS] " : "[FAIL] ";
            results.append(prefix);
            results.append(entry.getKey());
            final String message = entry.getValue().getMessage();
            if (!message.trim().isEmpty()) {
                results.append(" - ");
                results.append(message);
            }
                results.append("\n");
        }
        logger.warn("Validation result:\n\n" + results.toString());
    }
}
