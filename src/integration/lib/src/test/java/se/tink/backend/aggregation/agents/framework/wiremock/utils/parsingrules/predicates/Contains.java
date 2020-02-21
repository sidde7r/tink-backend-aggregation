package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates;

import java.util.function.Predicate;

public final class Contains implements Predicate<String> {

    private final String targetString;

    public Contains(String targetString) {
        this.targetString = targetString;
    }

    @Override
    public boolean test(String line) {
        return line.contains(targetString);
    }
}
