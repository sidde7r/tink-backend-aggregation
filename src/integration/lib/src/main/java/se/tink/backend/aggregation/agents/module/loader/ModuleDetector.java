package se.tink.backend.aggregation.agents.module.loader;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ModuleDetector {

    public ImmutableSet<Module> getModulesFromAnnotation(List<Class<? extends Module>> modules)
            throws NoSuchMethodException {
        try {
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
}
