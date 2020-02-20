package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.predicates;

import java.util.function.Predicate;

public final class IsEmptyResponsePrefix implements Predicate<String> {

    @Override
    public boolean test(String line) {
        String[] lineParts = line.split(" ");
        return lineParts.length == 2 && lineParts[1].contains("<");
    }
}
