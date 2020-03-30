package se.tink.backend.aggregation.agents.module.loader;

import com.google.inject.Module;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import org.reflections.Reflections;

public final class PackageModuleLoader {

    public Set<Module> getModulesInPackage(final String packagePath)
            throws ReflectiveOperationException {

        Reflections reflections = new Reflections(packagePath);
        Set<Class<? extends Module>> moduleClasses = reflections.getSubTypesOf(Module.class);

        Set<Module> moduleSet = new HashSet<>();
        for (Class<? extends Module> moduleClass : moduleClasses) {
            if (!Modifier.isAbstract(moduleClass.getModifiers())) {

                try {
                    moduleSet.add(moduleClass.getConstructor().newInstance());
                } catch (NoSuchMethodException e) {
                    throw new NoSuchMethodException(
                            String.format(
                                    "Could not find default constructor in module <%s>",
                                    moduleClass));
                }
            }
        }
        return moduleSet;
    }
}
