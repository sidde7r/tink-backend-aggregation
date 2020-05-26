package se.tink.backend.aggregation.agents.module.loader.testclasses;

import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForDecoupledMode;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;

@AgentDependencyModulesForProductionMode(modules = {InvalidModule.class})
@AgentDependencyModulesForDecoupledMode(modules = ValidModule.class)
@AgentDependencyModules(modules = ValidModule2.class)
public class TestAgentLoadingModulesForDifferentModes {}
