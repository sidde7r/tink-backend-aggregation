package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates;

import java.util.function.Predicate;

public class ContainsAny implements Predicate<String> {

    private final String[] targetStrings;

    public ContainsAny(String[] targetStrings) {
        this.targetStrings = targetStrings;
    }

    @Override
    public boolean test(String line) {
        for (String targetString : targetStrings) {
            if (line.contains(targetString)) {
                return true;
            }
        }
        return false;
    }
}
