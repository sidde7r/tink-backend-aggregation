package se.tink.backend.aggregation.agents.module.loader.testclasses;

import com.google.inject.AbstractModule;

public final class ValidModule extends AbstractModule {

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ValidModule;
    }

    @Override
    public int hashCode() {
        return ValidModule.class.hashCode();
    }
}
