package se.tink.backend.aggregation.agents.module.loader.incorrectmodule;

import com.google.inject.AbstractModule;

/** Module used to test AgentPackageModuleLoader */
public final class IncorrectModule extends AbstractModule {

    // Can't handle constructor parameters
    public IncorrectModule(int i) {}
}
