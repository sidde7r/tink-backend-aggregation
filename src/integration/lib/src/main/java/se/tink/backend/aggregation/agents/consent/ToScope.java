package se.tink.backend.aggregation.agents.consent;

@FunctionalInterface
public interface ToScope<T, U extends Scope> {

    U convert(T input);
}
