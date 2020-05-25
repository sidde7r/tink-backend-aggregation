package se.tink.backend.aggregation.agents.module.loader;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

public interface AgentDependencyModuleLoader {
    ImmutableSet<Module> getModulesFromAnnotation(final Class<?> agentClass)
            throws NoSuchMethodException;
}
