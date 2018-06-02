package se.tink.backend.main.guice.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import java.util.List;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.firehose.v1.guice.QueueProducerModule;
import se.tink.backend.guice.configuration.AbnAmroUserPropertiesBuilderModule;
import se.tink.backend.guice.configuration.AllRepositoryModule;
import se.tink.backend.guice.configuration.CommonModule;
import se.tink.backend.guice.configuration.ConfigurationModule;
import se.tink.backend.guice.configuration.EmailModule;
import se.tink.backend.guice.configuration.EventTrackerModule;
import se.tink.backend.guice.configuration.SmsModule;
import se.tink.backend.guice.configuration.UserPropertiesBuilderModule;
import se.tink.backend.main.guice.AuthenticationModule;
import se.tink.libraries.discovery.CoordinationModule;

public class MainModuleFactory {
    public static List<Module> build(ServiceConfiguration configuration, JerseyEnvironment jersey) {
        return ImmutableList.<Module>builder()
                .add(
                        new CommonModule(),
                        new ConfigurationModule(configuration),
                        new CoordinationModule(),
                        new EventTrackerModule(),
                        new MainServiceFactoryModule(),
                        new AllRepositoryModule(configuration.getDatabase(), configuration.getDistributedDatabase()),
                        new MainServiceModule(),
                        new MainJerseyModule(jersey),
                        new AuthenticationModule(configuration.getAuthentication().getMethods()),
                        new EmailModule(configuration.getEmail()),
                        new SmsModule(configuration.getSms()),
                        new QueueProducerModule())
                .addAll(clusterModules(configuration.getCluster()))
                .build();
    }

    private static ImmutableList<Module> clusterModules(Cluster cluster) {
        switch (cluster) {
        case ABNAMRO:
            return ImmutableList.of(new AbnAmroUserPropertiesBuilderModule());
        default:
            return ImmutableList.of(new UserPropertiesBuilderModule());
        }
    }
}
