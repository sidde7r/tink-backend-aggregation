package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class IsTimestamp implements Predicate<String> {

    // Match example '11 * 2020-01-30--13:44:10.064'.
    private static final Pattern TIMESTAMP_PATTERN =
            Pattern.compile("^\\d+ \\* \\d{4}-\\d{2}-\\d{2}--\\d{2}:\\d{2}:\\d{2}\\.\\d{3}");

    @Override
    public boolean test(String line) {
        return TIMESTAMP_PATTERN.matcher(line).matches();
    }
}
