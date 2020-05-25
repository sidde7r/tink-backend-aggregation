package se.tink.backend.aggregation.agents.module.loader.testclasses;

import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;

@AgentDependencyModulesForProductionMode(modules = InvalidModule.class)
public final class TestAgentLoadingInvalidModule {}
