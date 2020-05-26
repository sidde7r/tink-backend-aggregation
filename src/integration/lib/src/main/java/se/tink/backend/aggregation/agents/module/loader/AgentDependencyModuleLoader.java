package se.tink.backend.aggregation.agents.module.loader;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;

public abstract class AgentDependencyModuleLoader {

    private final Class<? extends Annotation> annotationClass;

    protected AgentDependencyModuleLoader(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    /**
     * Looks for <code>an annotation @annotationClass</code> and <code>AgentDependencyModules</code>
     * on agent and tries to instantiate any guice modules defined there.
     *
     * @param agentClass Class of agent to load modules for.
     * @return Set of all modules specified in <code>annotationClass</code> and <code>
     *     AgentDependencyModules</code> annotations on the agent.
     * @throws NoSuchMethodException if one or more modules does not have default constructor.
     */
    public ImmutableSet<Module> getModulesFromAnnotation(final Class<?> agentClass)
            throws NoSuchMethodException {
        try {
            List<Class<? extends Module>> modules = new ArrayList<>();
            if (agentClass.isAnnotationPresent(AgentDependencyModules.class)) {
                modules.addAll(
                        getModulesClassesFromAnnotation(
                                agentClass.getAnnotation(AgentDependencyModules.class)));
            }
            if (agentClass.isAnnotationPresent(annotationClass)) {
                modules.addAll(
                        getModulesClassesFromAnnotation(agentClass.getAnnotation(annotationClass)));
            }
            ImmutableSet.Builder<Module> setBuilder = ImmutableSet.builder();
            for (Class<? extends Module> moduleClass : modules) {
                setBuilder.add(moduleClass.getDeclaredConstructor().newInstance());
            }
            return setBuilder.build();
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException(
                    "Agent dependency module must have default constructor.");
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Error instantiating module.", e);
        }
    }

    private List<Class<? extends Module>> getModulesClassesFromAnnotation(Annotation annotation)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = annotation.annotationType().getDeclaredMethod("modules");
        Class<? extends Module>[] result = (Class<? extends Module>[]) method.invoke(annotation);
        return Arrays.asList(result);
    }
}
