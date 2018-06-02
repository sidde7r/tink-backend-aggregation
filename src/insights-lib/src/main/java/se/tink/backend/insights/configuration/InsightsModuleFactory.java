package se.tink.backend.insights.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import se.tink.backend.guice.configuration.CommonModule;
import se.tink.backend.guice.configuration.UserPropertiesBuilderModule;
import se.tink.libraries.discovery.CoordinationModule;

public class InsightsModuleFactory {
    public static Iterable<Module> build(ActionableInsightsConfiguration configuration, JerseyEnvironment environment) {
        return ImmutableList.<Module>builder()
                .add(
                        new CommonModule(),
                        new CoordinationModule(),
                        new InsightsConfigurationModule(configuration),
                        new InsightsRepositoryModule(configuration.getDatabase(),
                                configuration.getDistributedDatabase()),
                        new InsightsServiceModule(environment),
                        new SessionAuthenticationModule(configuration.getEmailConfiguration().getMandrillApiKey(), configuration.getAuthentication().getMethods()),
                        new UserPropertiesBuilderModule())
                .build();
    }
}
