package se.tink.backend.aggregation.agents.module.loader;

import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;

public final class AgentDependencyModuleLoaderForProduction extends AgentDependencyModuleLoader {

    public AgentDependencyModuleLoaderForProduction() {
        super(AgentDependencyModulesForProductionMode.class);
    }
}
