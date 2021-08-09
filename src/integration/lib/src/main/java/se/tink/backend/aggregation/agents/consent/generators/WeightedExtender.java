package se.tink.backend.aggregation.agents.consent.generators;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.consent.Scope.Weighted;

@RequiredArgsConstructor
public class WeightedExtender<WEIGHTED extends Weighted> {

    private final Collection<WEIGHTED> weightedScopes;

    public WEIGHTED extendIfNotAvailable(WEIGHTED weightedScope, Set<WEIGHTED> availableScopes) {

        WEIGHTED current = weightedScope;
        while (!availableScopes.contains(current)) {
            current =
                    getNext(current)
                            .orElseThrow(
                                    () ->
                                            new RuntimeException(
                                                    String.format(
                                                            "Cannot extend weighted %s to %s",
                                                            weightedScope, availableScopes)));
        }

        return current;
    }

    private Optional<WEIGHTED> getNext(WEIGHTED current) {
        return weightedScopes.stream()
                .filter(weightedScope -> current.getWeight() < weightedScope.getWeight())
                .min(Comparator.comparing(Weighted::getWeight));
    }
}
