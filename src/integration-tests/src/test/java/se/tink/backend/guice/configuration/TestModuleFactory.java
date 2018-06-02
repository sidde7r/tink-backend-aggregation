package se.tink.backend.guice.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import java.util.List;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.main.guice.AuthenticationModule;
import se.tink.backend.main.guice.configuration.MainServiceModule;
import se.tink.backend.system.guice.configuration.SystemProcessingModule;
import se.tink.backend.system.guice.configuration.SystemServiceModule;
import se.tink.libraries.discovery.CoordinationModule;

public class TestModuleFactory {
    public static List<Module> getDefaultModules(ServiceConfiguration configuration) {
        return ImmutableList.of(
                new CommonModule(),
                new SystemServiceModule(),
                new ConfigurationModule(configuration),
                new SystemProcessingModule(configuration),
                new TestServicesModule(),
                new CoordinationModule(),
                new TestTrackerModule(),
                new AllRepositoryModule(configuration.getDatabase(), configuration.getDistributedDatabase()),
                new MainServiceModule(),
                new AuthenticationModule(configuration.getAuthentication().getMethods()),
                new SmsModule(configuration.getSms()),
                new EmailModule(configuration.getEmail()),
                new UserPropertiesBuilderModule());
    }
}
