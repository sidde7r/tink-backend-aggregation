package se.tink.backend.aggregation.agents.module.loader.correctnestedmodules.subpackage;

import com.google.inject.AbstractModule;

/** Empty module used to test reflection logic in AgentPackageModuleLoader. */
public final class SubPackageModule extends AbstractModule {

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SubPackageModule;
    }
}
