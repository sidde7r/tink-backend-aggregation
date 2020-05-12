package se.tink.backend.aggregation.agents.module.loader.correctnestedmodules;

import se.tink.backend.aggregation.agents.module.loader.TestModule;

public final class TestModuleImpl extends TestModule {
    @Override
    public int hashCode() {
        return 3;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TestModuleImpl;
    }
}
