package se.tink.backend.aggregation.agents.module.loader.testclasses;

import com.google.inject.AbstractModule;

public final class ValidModule2 extends AbstractModule {

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ValidModule2;
    }

    @Override
    public int hashCode() {
        return ValidModule2.class.hashCode();
    }
}
