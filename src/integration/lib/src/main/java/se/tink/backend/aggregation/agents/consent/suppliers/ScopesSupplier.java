package se.tink.backend.aggregation.agents.consent.suppliers;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.consent.Scope;
import se.tink.backend.aggregation.agents.consent.ToScopes;

@Slf4j
@RequiredArgsConstructor
public class ScopesSupplier<T, U extends Scope> implements Supplier<Set<U>> {

    private final Collection<T> inputCollection;
    private final Set<U> availableScopes;
    private final ToScopes<T, U> toScopes;

    @Override
    public Set<U> get() {
        Set<U> scopes =
                inputCollection.stream()
                        .map(toScopes::convert)
                        .flatMap(Collection::stream)
                        .filter(availableScopes::contains)
                        .collect(Collectors.toCollection(TreeSet::new));

        log.info(
                "[CONSENT GENERATOR] inputCollection: {}, availableScopes: {} -> {}",
                inputCollection,
                availableScopes,
                scopes);
        return scopes;
    }

    public Set<String> getStrings() {
        return get().stream().map(U::toString).collect(Collectors.toCollection(TreeSet::new));
    }
}
