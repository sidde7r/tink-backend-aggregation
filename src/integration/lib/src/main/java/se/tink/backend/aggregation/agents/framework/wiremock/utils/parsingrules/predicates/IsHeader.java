package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class IsHeader implements Predicate<String> {

    // Match example '11 > Keep-Alive: ***
    private static final Pattern HEADER_PATTERN = Pattern.compile("^\\d+ [><] .*?:.*");

    @Override
    public boolean test(String line) {
        return HEADER_PATTERN.matcher(line).matches();
    }
}
