package se.tink.backend.connector;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.google.inject.Injector;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.ResourceConfig;
import io.dropwizard.jersey.validation.ConstraintViolationExceptionMapper;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import javax.ws.rs.Path;
import org.apache.commons.lang.NotImplementedException;
import org.apache.curator.framework.CuratorFramework;
import se.tink.backend.common.AbstractServiceContainer;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.connector.cli.ConnectorBenchmarkCommand;
import se.tink.backend.connector.cli.SEBConnectorBenchmarkCommand;
import se.tink.backend.connector.cli.SebReplayTransactionCommand;
import se.tink.backend.connector.configuration.ConnectorModulesFactory;
import se.tink.backend.connector.response.ConstraintViolationResponseMapper;
import se.tink.backend.connector.response.RequestErrorResponseMapper;
import se.tink.libraries.discovery.ServiceDiscoveryHelper;
import se.tink.libraries.dropwizard.DropwizardLifecycleInjectorFactory;
import se.tink.libraries.dropwizard.DropwizardObjectMapperConfigurator;

@Path("/connector")
public class ConnectorServiceContainer extends AbstractServiceContainer {

    public static final String SERVICE_NAME = "connector";

    public static void main(String[] args) throws Exception {
        new ConnectorServiceContainer().run(args);
    }

    @Override
    public String getName() {
        return SERVICE_NAME;
    }

    @Override
    public void initialize(Bootstrap<ServiceConfiguration> bootstrap) {
        DropwizardObjectMapperConfigurator.doNotFailOnUnknownProperties(bootstrap);
        bootstrap.addCommand(new ConnectorBenchmarkCommand());
        bootstrap.addCommand(new SEBConnectorBenchmarkCommand());
        bootstrap.addCommand(new SebReplayTransactionCommand());
    }

    @Override
    protected void build(ServiceConfiguration configuration, Environment environment) {
        if (configuration.getServiceAuthentication() != null
                && !configuration.getServiceAuthentication().getServerTokens().isEmpty()) {
            throw new NotImplementedException(
                    "Using server tokens isn't supported for connector. It will block traffic from external integration points.");
        }
        buildContainer(configuration, environment, DropwizardLifecycleInjectorFactory.build(
                environment.lifecycle(),
                ConnectorModulesFactory.build(configuration, environment.jersey())));
    }

    @SuppressWarnings("unchecked")
    private void buildContainer(ServiceConfiguration configuration, Environment environment, Injector injector) {

        // Makes it possible to intercept bad requests where field constraints or prerequisites are violated.
        environment.jersey().getResourceConfig().getSingletons()
                .removeIf(singleton -> singleton instanceof ConstraintViolationExceptionMapper);
        environment.jersey().register(new ConstraintViolationResponseMapper());
        environment.jersey().register(new RequestErrorResponseMapper());

        environment.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ServiceDiscoveryHelper serviceDiscoveryHelper = constructServiceDiscoveryHelperFromConfiguration(
                injector.getInstance(CuratorFramework.class), configuration,
                SERVICE_NAME);

        environment.lifecycle().manage(serviceDiscoveryHelper);

        if (configuration.isDebugMode()) {
            // Log whole request including full headers and body
            environment.jersey().property(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                    LoggingFilter.class.getName());
        }
    }
}

