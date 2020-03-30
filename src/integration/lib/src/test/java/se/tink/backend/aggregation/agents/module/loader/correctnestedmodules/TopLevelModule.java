package se.tink.backend.aggregation.agents.module.loader.correctnestedmodules;

import com.google.inject.AbstractModule;

/** Empty module used to test reflection logic in AgentPackageModuleLoader. */
public final class TopLevelModule extends AbstractModule {

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TopLevelModule;
    }
}
