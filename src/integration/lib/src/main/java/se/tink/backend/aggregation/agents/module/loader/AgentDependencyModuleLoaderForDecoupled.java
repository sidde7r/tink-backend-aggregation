package se.tink.backend.aggregation.agents.module.loader;

import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForDecoupledMode;

public final class AgentDependencyModuleLoaderForDecoupled extends AgentDependencyModuleLoader {

    public AgentDependencyModuleLoaderForDecoupled() {
        super(AgentDependencyModulesForDecoupledMode.class);
    }
}
