package se.tink.backend.aggregation.agents.module.loader;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import java.lang.reflect.InvocationTargetException;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;

public final class AgentDependencyModuleLoaderForProduction implements AgentDependencyModuleLoader {

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

        if (agentClass.isAnnotationPresent(AgentDependencyModulesForProductionMode.class)) {
            final AgentDependencyModulesForProductionMode moduleAnnotation =
                    agentClass.getAnnotation(AgentDependencyModulesForProductionMode.class);

            try {

                ImmutableSet.Builder<Module> setBuilder = ImmutableSet.builder();
                for (Class<? extends Module> moduleClass : moduleAnnotation.modules()) {

                    setBuilder.add(moduleClass.getDeclaredConstructor().newInstance());
                }

                return setBuilder.build();
            } catch (NoSuchMethodException e) {

                throw new NoSuchMethodException(
                        "Agent dependency module must have default constructor.");
            } catch (InstantiationException
                    | IllegalAccessException
                    | InvocationTargetException e) {

                throw new IllegalStateException("Error instantiating module.", e);
            }
        }

        // No modules loaded.
        return ImmutableSet.of();
    }
}
