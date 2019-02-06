package se.tink.backend.aggregation.nxgen.framework.validation;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WarnExecutor implements ValidationExecutor {
    private static final Logger logger = LoggerFactory.getLogger(WarnExecutor.class);

    @Override
    public void execute(final ValidationResult result) {
        final boolean allPassed =
                result.getSubResults().values().stream().allMatch(ValidationSubResult::passed);

        if (allPassed) {
            final int ruleCount = result.getSubResults().size();
            final String resultString = String.format("%d/%d rules passed.", ruleCount, ruleCount);
            logger.info("Validation result:\n" + resultString + "\n");
            return;
        }

        final StringBuilder results = new StringBuilder();
        for (final Map.Entry<String, ValidationSubResult> entry :
                result.getSubResults().entrySet()) {
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
