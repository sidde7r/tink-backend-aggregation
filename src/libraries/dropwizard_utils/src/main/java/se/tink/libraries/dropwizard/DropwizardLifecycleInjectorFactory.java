package se.tink.libraries.dropwizard;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;

public class DropwizardLifecycleInjectorFactory {
    public static Injector build(LifecycleEnvironment lifecycle, Iterable<Module> modules) {
        Injector injector =
                LifecycleInjector.builder()
                        .inStage(Stage.PRODUCTION)
                        .withModules(modules)
                        .build()
                        .createInjector();
        lifecycle.manage(new ManagedLifecycleManager(injector.getInstance(LifecycleManager.class)));
        return injector;
    }
}
