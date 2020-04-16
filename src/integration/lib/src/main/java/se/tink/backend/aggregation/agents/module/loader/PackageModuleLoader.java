package se.tink.backend.aggregation.agents.module.loader;

import com.google.inject.Module;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class PackageModuleLoader {

    public Set<Module> getModulesInPackage(final String packagePath)
            throws ReflectiveOperationException {

        final Collection<Class<Module>> moduleClasses;
        try (final ScanResult scanResult =
                new ClassGraph().enableClassInfo().whitelistPackages(packagePath).scan()) {
            final ClassInfoList classInfoList =
                    scanResult
                            .getClassesImplementing(Module.class.getName())
                            .filter(c -> !c.isAbstract());
            moduleClasses = classInfoList.loadClasses(Module.class);
        }

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
