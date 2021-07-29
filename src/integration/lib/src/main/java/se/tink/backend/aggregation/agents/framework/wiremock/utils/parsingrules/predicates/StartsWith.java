package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates;

import java.util.function.Predicate;

public class StartsWith implements Predicate<String> {
    private final String prefix;

    public StartsWith(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean test(String s) {
        return s.startsWith(prefix);
    }
}
