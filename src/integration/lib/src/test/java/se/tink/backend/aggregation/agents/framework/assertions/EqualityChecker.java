package se.tink.backend.aggregation.agents.framework.assertions;

public interface EqualityChecker<T> {
    boolean isEqual(T obj1, T obj2);
}
