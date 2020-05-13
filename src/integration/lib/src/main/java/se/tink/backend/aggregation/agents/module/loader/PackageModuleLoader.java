package se.tink.backend.aggregation.agents.module.loader;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

public final class PackageModuleLoader {

    public Set<Module> getModulesInPackage(final String packagePath) {
        Set<Module> result = new HashSet<>();

        try {
            String modulePath = packagePath + ".module.AgentModule";
            Class<AbstractModule> classDefinition =
                    (Class<AbstractModule>) Class.forName(modulePath);
            Module module = classDefinition.getDeclaredConstructor().newInstance();
            result.add(module);
        } catch (ClassNotFoundException
                | IllegalAccessException
                | InstantiationException
                | NoSuchMethodException
                | InvocationTargetException e) {
            // NOOP (it is normal not to find any module, not all agents use a custom module)
        }
        return result;
    }
}
