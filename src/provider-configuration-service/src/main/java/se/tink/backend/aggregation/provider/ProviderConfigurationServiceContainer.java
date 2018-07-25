package se.tink.backend.aggregation.provider;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import se.tink.backend.aggregation.provider.configuration.config.ProviderModuleFactory;
import se.tink.backend.common.AbstractServiceContainer;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.libraries.auth.ApiTokenAuthorizationHeaderPredicate;
import se.tink.libraries.auth.ContainerAuthorizationResourceFilterFactory;
import se.tink.libraries.auth.YubicoAuthorizationHeaderPredicate;
import se.tink.libraries.dropwizard.DropwizardLifecycleInjectorFactory;
import se.tink.libraries.dropwizard.DropwizardObjectMapperConfigurator;

public class ProviderConfigurationServiceContainer extends AbstractServiceContainer {

    public static void main(String[] args) throws Exception {
        new ProviderConfigurationServiceContainer().run(args);
    }

    @Override
    public String getName() {
        return "PROVIDER_SERVICE";
    }

    @Override
    public void initialize(Bootstrap<ServiceConfiguration> bootstrap) {
        DropwizardObjectMapperConfigurator.doNotFailOnUnknownProperties(bootstrap);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void build(ServiceConfiguration configuration, Environment environment) throws Exception {
        if (!configuration.isAggregationCluster() && configuration.getServiceAuthentication() != null
                && !configuration.getServiceAuthentication().getServerTokens().isEmpty()) {
            Predicate<String> authorizationAuthorizers = Predicates.or(
                    new ApiTokenAuthorizationHeaderPredicate(configuration.getServiceAuthentication()
                            .getServerTokens()),
                    new YubicoAuthorizationHeaderPredicate(
                            configuration.getYubicoClientId(),
                            configuration.getServiceAuthentication().getYubikeys()));
            environment.jersey().getResourceConfig().getResourceFilterFactories()
                            .add(new ContainerAuthorizationResourceFilterFactory(authorizationAuthorizers));
        }

        Injector injector = DropwizardLifecycleInjectorFactory.build(
                environment.lifecycle(),
                ProviderModuleFactory.build(configuration, environment.jersey()));
    }
}
