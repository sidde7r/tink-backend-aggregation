package se.tink.backend.aggregation.agents.module.loader.testclasses;

import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;

@AgentDependencyModules(modules = {ValidModule.class, ValidModule2.class})
public final class TestAgentLoadingTwoModules {}
