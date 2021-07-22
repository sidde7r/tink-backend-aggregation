package se.tink.backend.aggregation.agents.consent;

import java.util.Set;

public interface Scope {

    String toString();

    interface Weighted<T> extends Scope {

        int getWeight();

        T extendIfNotAvailable(Set<T> availableScopes);
    }
}
