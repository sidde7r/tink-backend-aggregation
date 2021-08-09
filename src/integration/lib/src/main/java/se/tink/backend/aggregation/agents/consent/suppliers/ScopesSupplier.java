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
public class ScopesSupplier<INPUT_TYPE, SCOPE extends Scope> implements Supplier<Set<SCOPE>> {

    private final Collection<INPUT_TYPE> inputCollection;
    private final Set<SCOPE> availableScopes;
    private final ToScopes<INPUT_TYPE, SCOPE> toScopes;

    @Override
    public Set<SCOPE> get() {
        Set<SCOPE> scopes =
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
        return get().stream().map(SCOPE::toString).collect(Collectors.toCollection(TreeSet::new));
    }
}
