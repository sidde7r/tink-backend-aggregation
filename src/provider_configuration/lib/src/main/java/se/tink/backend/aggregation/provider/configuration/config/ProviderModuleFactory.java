package se.tink.backend.aggregation.provider.configuration.config;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import se.tink.backend.aggregation.provider.configuration.storage.module.ProviderFileModule;

public class ProviderModuleFactory {
    public static ImmutableList<Module> build(ProviderServiceConfiguration configuration, JerseyEnvironment jersey) {
        return ImmutableList.of(
                new ProviderServiceConfigurationModule(configuration),
                new ProviderServiceModule(jersey),
                new ProviderFileModule(),
                new ProviderRepositoryModule(configuration.getDatabase()));
    }
}
