package se.tink.backend.export.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import se.tink.libraries.discovery.CoordinationModule;

public class ExportUserDataModuleFactory {
    public static Iterable<Module> build(ExportUserDataConfiguration configuration, JerseyEnvironment environment) {
        return ImmutableList.<Module>builder()
                .add(
                        new LibraryModule(),
                        new CoordinationModule(),
                        new ExportUserDataConfigurationModule(configuration),
                        new ExportUserDataRepositoryModule(configuration.getDatabase(),
                                configuration.getDistributedDatabase()),
                        new ExportUserDataModule(),
                        new ExportServiceModule(environment)
                )
                .build();
    }
}
