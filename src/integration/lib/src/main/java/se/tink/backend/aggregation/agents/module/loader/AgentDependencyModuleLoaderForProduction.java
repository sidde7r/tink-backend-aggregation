package se.tink.backend.aggregation.agents.module.loader;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;

public final class AgentDependencyModuleLoaderForProduction implements AgentDependencyModuleLoader {

    private static final ModuleDetector moduleDetector = new ModuleDetector();

    /**
     * Looks for <code>@AgentDependencyModulesForProduction</code> on agent and tries to instantiate
     * any guice modules defined there.
     *
     * @param agentClass Class of agent to load modules for.
     * @return Set of all modules specified in <code>@AgentDependencyModulesForProduction</code>
     *     annotation on the agent.
     * @throws NoSuchMethodException if one or more modules does not have default constructor.
     */
    @Override
    public ImmutableSet<Module> getModulesFromAnnotation(final Class<?> agentClass)
            throws NoSuchMethodException {
        List<Class<? extends Module>> modules = new ArrayList<>();
        if (agentClass.isAnnotationPresent(AgentDependencyModules.class)) {
            final AgentDependencyModules moduleAnnotation =
                    agentClass.getAnnotation(AgentDependencyModules.class);
            modules.addAll(Arrays.asList(moduleAnnotation.modules()));
        }
        if (agentClass.isAnnotationPresent(AgentDependencyModulesForProductionMode.class)) {
            final AgentDependencyModulesForProductionMode moduleAnnotation =
                    agentClass.getAnnotation(AgentDependencyModulesForProductionMode.class);
            modules.addAll(Arrays.asList(moduleAnnotation.modules()));
        }
        return moduleDetector.getModulesFromAnnotation(modules);
    }
}
