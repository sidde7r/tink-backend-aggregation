package se.tink.backend.aggregation.agents.consent.suppliers;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.consent.Scope.Weighted;
import se.tink.backend.aggregation.agents.consent.ToScope;

@Slf4j
@RequiredArgsConstructor
public class WeightedScopeSupplier<INPUT_TYPE, WEIGHTED extends Weighted<WEIGHTED>>
        implements Supplier<WEIGHTED> {

    private final Collection<INPUT_TYPE> inputCollection;
    private final Set<WEIGHTED> availableScopes;
    private final ToScope<INPUT_TYPE, WEIGHTED> toScope;

    @Override
    public WEIGHTED get() {
        WEIGHTED outputScope =
                inputCollection.stream()
                        .map(toScope::convert)
                        .max(Comparator.comparing(WEIGHTED::getWeight))
                        .map(scope -> scope.extendIfNotAvailable(availableScopes))
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "[CONSENT GENERATOR] inputCollection: "
                                                        + inputCollection
                                                        + ", availableScopes: "
                                                        + availableScopes
                                                        + " -> Failed to generate scope"));

        log.info(
                "[CONSENT GENERATOR] inputCollection: {}, availableScopes: {} -> {}",
                inputCollection,
                availableScopes,
                outputScope);
        return outputScope;
    }
}
