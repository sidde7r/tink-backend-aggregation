package se.tink.backend.product.execution.integration.configurations;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import org.mockito.Mockito;
import se.tink.backend.product.execution.configuration.ProductExecutorConfiguration;
import se.tink.backend.product.execution.configuration.ProductExecutorModuleFactory;

public class InjectorFactory {
    private static Injector injector;

    public static synchronized Injector get(ProductExecutorConfiguration configuration) {
        if (injector == null) {
            injector = Guice.createInjector(
                    Modules.override(
                            ProductExecutorModuleFactory.build(configuration, Mockito.mock(JerseyEnvironment.class)))
                            .with(new TestServiceModule()));
        }

        return injector;
    }

    public static synchronized Injector get(String configPath) {
        ProductExecutorConfiguration configuration = ConfigurationFactory.get(configPath);
        return get(configuration);
    }
}
