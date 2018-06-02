package se.tink.backend.system.guice.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import java.util.List;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.guice.configuration.AllRepositoryModule;
import se.tink.backend.guice.configuration.CommonModule;
import se.tink.backend.guice.configuration.ConfigurationModule;
import se.tink.backend.guice.configuration.EmailModule;
import se.tink.backend.guice.configuration.EventTrackerModule;
import se.tink.libraries.discovery.CoordinationModule;

public class SystemModuleFactory {
    public static List<AbstractModule> build(ServiceConfiguration configuration,
            JerseyEnvironment jersey) {
        return ImmutableList.of(
                new CommonModule(),
                new ConfigurationModule(configuration),
                new CoordinationModule(),
                new EventTrackerModule(),
                new SystemServiceModule(),
                new SystemProcessingModule(configuration),
                new SystemJerseyModule(jersey),
                new SystemServiceFactoryModule(),
                new AllRepositoryModule(configuration.getDatabase(), configuration.getDistributedDatabase()),
                new EmailModule(configuration.getEmail()));
    }
}
