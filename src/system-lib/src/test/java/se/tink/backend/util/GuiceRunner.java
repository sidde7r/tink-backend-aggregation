package se.tink.backend.util;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import se.tink.backend.system.guice.SystemTestModuleFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GuiceRunner extends BlockJUnit4ClassRunner{
    private final Injector injector;

    public GuiceRunner(Class<?> klass) throws InitializationError, IOException{
        super(klass);
        List<Class<?>> moduleClasses = getModulesForTest(klass);
        List<Module> modules = createModule(moduleClasses);
        injector = createInjector(modules);
    }

    @Override
    protected Object createTest() throws Exception {
        Object test = super.createTest();

        if(injector != null)
            injector.injectMembers(test);

        return test;
    }

    private Injector createInjector(List<Module> modules) {
        List<Module> moduleList = Stream.concat(SystemTestModuleFactory.getDefaultModules().stream(), modules.stream())
                .collect(Collectors.toList());
        return Guice.createInjector(moduleList);
    }

    private List<Module> createModule(List<Class<?>> classes) {
        return classes.stream()
                .map(clzz -> {
                    Module module = null;
                    try {
                        module = (Module) clzz.newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } finally {
                        return module;
                    }
                }).collect(Collectors.toList());
    }

    private List<Class<?>> getModulesForTest(Class<?> testClass) {
        Optional<GuiceModules> modulesOptional = Optional.ofNullable(testClass.getAnnotation(GuiceModules.class));
        if (modulesOptional.isPresent())
            return Arrays.stream(modulesOptional.get().value()).collect(Collectors.toList());
        else
            return Collections.emptyList();
    }
}
