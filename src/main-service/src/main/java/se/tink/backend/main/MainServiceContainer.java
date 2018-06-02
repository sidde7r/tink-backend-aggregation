package se.tink.backend.main;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.dropwizard.cli.Command;
import io.dropwizard.jersey.validation.ConstraintViolationExceptionMapper;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang.NotImplementedException;
import se.tink.backend.client.CorsFilterAdder;
import se.tink.backend.client.InProcessServiceFactoryBuilder;
import se.tink.backend.client.ServiceFactory;
import se.tink.backend.common.AbstractServiceContainer;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.admin.DrainModeTask;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.providers.OAuth2ClientProvider;
import se.tink.backend.common.repository.mysql.main.OAuth2ClientRepository;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.grpc.v1.guice.configuration.GrpcModule;
import se.tink.backend.grpc.v1.guice.configuration.queue.QueueConsumerModule;
import se.tink.backend.main.auth.JerseyAuthenticationProvider;
import se.tink.backend.main.auth.exceptions.jersey.JerseyUnauthorizedDeviceResponseMapper;
import se.tink.backend.main.auth.exceptions.jersey.JerseyUnsupportedClientResponseMapper;
import se.tink.backend.main.commands.AddOauth2ClientCommand;
import se.tink.backend.main.guice.configuration.MainModuleFactory;
import se.tink.backend.main.providers.ClientProvider;
import se.tink.backend.main.rpc.OAuth2ClientRequestEnricher;
import se.tink.backend.main.rpc.RequestEnricher;
import se.tink.backend.response.jersey.JerseyConstraintViolationResponseMapper;
import se.tink.backend.response.jersey.JerseyRequestErrorResponseMapper;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.discovery.ServiceDiscoveryHelper;
import se.tink.libraries.dropwizard.DropwizardLifecycleInjectorFactory;
import se.tink.libraries.dropwizard.DropwizardObjectMapperConfigurator;
import se.tink.libraries.metrics.MetricRegistry;

public class MainServiceContainer extends AbstractServiceContainer {

    private static final ImmutableList<Command> commands = ImmutableList.of(new AddOauth2ClientCommand());

    public static void main(String[] args) throws Exception {
        new MainServiceContainer().run(args);
    }

    @Override
    public String getName() {
        return ServiceFactory.SERVICE_NAME;
    }

    @Override
    public void initialize(Bootstrap<ServiceConfiguration> bootstrap) {
        DropwizardObjectMapperConfigurator.doNotFailOnUnknownProperties(bootstrap);
        commands.forEach(bootstrap::addCommand);
    }

    @Override
    protected void build(ServiceConfiguration configuration, Environment environment) throws Exception {

        if (configuration.getServiceAuthentication() != null
                && !configuration.getServiceAuthentication().getServerTokens().isEmpty()) {
            throw new NotImplementedException(
                    "Using server tokens isn't necessary for main. It has its own authentication logic.");
        }

        List<Module> modules = Lists.newArrayList();
        if (configuration.getGrpc().isEnabled()) {
            modules.add(new GrpcModule(configuration.getCluster(), configuration.getInsightsConfiguration().isEnabled()));
            modules.add(new QueueConsumerModule());
        }

        modules.addAll(MainModuleFactory.build(configuration, environment.jersey()));
        Injector injector = DropwizardLifecycleInjectorFactory.build(environment.lifecycle(), modules);

        ServiceContext serviceContext = injector.getInstance(ServiceContext.class);
        environment.admin().addTask(injector.getInstance(DrainModeTask.class));
        buildContainer(configuration, environment, serviceContext, injector);
    }

    @SuppressWarnings("unchecked")
    private void buildContainer(ServiceConfiguration configuration, Environment environment,
            ServiceContext serviceContext, Injector injector) {

        ClientProvider clientProvider = new ClientProvider(configuration.getCluster());
        OAuth2ClientProvider oauth2ClientProvider = new OAuth2ClientProvider(
                serviceContext.getRepository(OAuth2ClientRepository.class));
        OAuth2ClientRequestEnricher oAuth2ClientRequestEnricher = new OAuth2ClientRequestEnricher(oauth2ClientProvider,
                clientProvider);

        // Makes it possible to intercept bad requests where field constraints or prerequisites are violated.
        environment.jersey().getResourceConfig().getSingletons()
                .removeIf(singleton -> singleton instanceof ConstraintViolationExceptionMapper);
        environment.jersey().register(new JerseyConstraintViolationResponseMapper());

        environment.jersey().register(new RequestEnricher(oAuth2ClientRequestEnricher));
        environment.jersey().register(injector.getInstance(JerseyAuthenticationProvider.class));
        environment.jersey().register(new JerseyRequestErrorResponseMapper());
        environment.jersey().register(new JerseyUnauthorizedDeviceResponseMapper());
        environment.jersey().register(new JerseyUnsupportedClientResponseMapper());

        InProcessServiceFactoryBuilder inProcessServiceFactoryBuilder = new InProcessServiceFactoryBuilder(
                serviceContext, Optional.of(environment), injector.getInstance(MetricRegistry.class),
                injector.getInstance(ClusterCategories.class));

        inProcessServiceFactoryBuilder.buildAndRegister(injector);

        final ServiceDiscoveryHelper serviceDiscoveryService = constructServiceDiscoveryHelperFromConfiguration(
                serviceContext.getCoordinationClient(), configuration, ServiceFactory.SERVICE_NAME);

        environment.lifecycle().manage(serviceDiscoveryService);

        if (Objects.equals(configuration.getCluster(), Cluster.TINK)) {
            CorsFilterAdder corsFilterAdder = new CorsFilterAdder(serviceContext);
            corsFilterAdder.add(environment);
        }
    }
}
