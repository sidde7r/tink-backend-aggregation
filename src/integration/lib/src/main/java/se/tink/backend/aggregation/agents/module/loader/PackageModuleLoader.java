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

    /*
       This method is synchronised because we observed some problematic behaviour if we call this
       code block by multiple threads. In such case, sometimes the scan method cannot detect anything
       To reproduce the issue, remove "synchronized" keyword and run AgentInitialisationTest.java
       where initialiseAgent method are run in parallel (you might need to run it multiple times
       to observe the issue)
    */
    private synchronized ScanResult getScanResult(final String packagePath) {
        return new ClassGraph().enableClassInfo().whitelistPackages(packagePath).scan();
    }

    public Set<Module> getModulesInPackage(final String packagePath)
            throws ReflectiveOperationException {

        final Collection<Class<Module>> moduleClasses;
        try (final ScanResult scanResult = getScanResult(packagePath)) {
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
