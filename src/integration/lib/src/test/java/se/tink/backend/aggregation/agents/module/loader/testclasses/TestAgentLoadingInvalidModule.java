package se.tink.backend.aggregation.agents.module.loader.testclasses;

import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;

@AgentDependencyModules(modules = InvalidModule.class)
public final class TestAgentLoadingInvalidModule {}
