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
public class WeightedScopeSupplier<T, U extends Weighted<U>> implements Supplier<U> {

    private final Collection<T> inputCollection;
    private final Set<U> availableScopes;
    private final ToScope<T, U> toScope;

    @Override
    public U get() {
        U outputScope =
                inputCollection.stream()
                        .map(toScope::convert)
                        .max(Comparator.comparing(U::getWeight))
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
