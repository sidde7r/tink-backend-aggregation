package se.tink.backend.aggregation.agents.consent;

import java.util.Set;

@FunctionalInterface
public interface ToScopes<T, U extends Scope> {

    Set<U> convert(T input);
}
