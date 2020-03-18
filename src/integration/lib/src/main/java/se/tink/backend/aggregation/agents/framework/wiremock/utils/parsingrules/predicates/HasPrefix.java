package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class HasPrefix implements Predicate<String> {

    // Match example '11 * 2020-01-30--13:44:10.064'.
    private static final Pattern PREFIX_PATTERN = Pattern.compile("^\\d+ [*<>] ");

    @Override
    public boolean test(String line) {
        return PREFIX_PATTERN.matcher(line).find();
    }
}
