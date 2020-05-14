package se.tink.backend.aggregation.agents.module.loader.testclasses;

import com.google.inject.AbstractModule;

/** Module used to test AgentPackageModuleLoader */
public final class InvalidModule extends AbstractModule {

    // Can't handle constructor parameters
    public InvalidModule(int i) {}

    @Override
    public boolean equals(Object obj) {
        return obj instanceof InvalidModule;
    }

    @Override
    public int hashCode() {
        return InvalidModule.class.hashCode();
    }
}
