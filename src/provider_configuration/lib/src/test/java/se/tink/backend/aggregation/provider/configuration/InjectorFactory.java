package se.tink.backend.aggregation.provider.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import se.tink.backend.aggregation.provider.configuration.config.ProviderServiceConfiguration;
import se.tink.backend.aggregation.provider.configuration.config.ProviderServiceConfigurationModule;
import se.tink.backend.aggregation.provider.configuration.storage.module.ProviderFileModule;

public class InjectorFactory {
    private static Injector injector;

    public static synchronized Injector get(ProviderServiceConfiguration configuration) {
        if (injector == null) {
            injector = Guice.createInjector(build(configuration));
        }

        return injector;
    }

    public static synchronized Injector get(String configPath) {
        ProviderServiceConfiguration configuration = ProviderConfigurationFactory.get(configPath);
        return get(configuration);
    }

    private static Iterable<Module> build(ProviderServiceConfiguration configuration) {
        return ImmutableList.<Module>builder()
                .add(
                        new ProviderServiceConfigurationModule(configuration),
                        new TestServiceModule(),
                        new ProviderFileModule(),
                        new TestRepositoryModule())
                .build();
    }
}
