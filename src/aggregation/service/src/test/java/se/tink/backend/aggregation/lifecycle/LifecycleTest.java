package se.tink.backend.aggregation.lifecycle;

import static org.mockito.Mockito.*;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.SyslogAppenderFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.setup.Environment;
import java.io.IOException;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validation;
import javax.validation.Validator;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import se.tink.backend.aggregation.AggregationServiceContainer;
import se.tink.backend.aggregation.configuration.ConfigurationValidator;
import se.tink.backend.aggregation.configuration.DevelopmentConfigurationSeeder;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;
import se.tink.backend.aggregation.storage.database.daos.CryptoConfigurationDao;
import se.tink.backend.aggregation.workers.AgentWorker;
import se.tink.backend.integration.agent_data_availability_tracker.client.AgentDataAvailabilityTrackerClient;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.libraries.draining.DrainModeTask;

public class LifecycleTest {
    private DefaultServerFactory serverFactory;
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private AggregationServiceConfiguration configuration;
    private AggregationServiceContainer aggregationContainer;
    private ManagedTppSecretsServiceClient managedTppSecretsServiceClient;
    private AgentWorker agentWorker;

    @Before
    public void setUp() throws IOException, ConfigurationException {
        objectMapper
                .getSubtypeResolver()
                .registerSubtypes(
                        ConsoleAppenderFactory.class,
                        FileAppenderFactory.class,
                        SyslogAppenderFactory.class,
                        HttpConnectorFactory.class);
        this.serverFactory =
                new ConfigurationFactory<>(
                                DefaultServerFactory.class,
                                Validation.buildDefaultValidatorFactory().getValidator(),
                                objectMapper,
                                "dw")
                        .build();
        configuration = new AggregationServiceConfiguration();
        Injector injector = Mockito.mock(Injector.class);

        setUpInjectorMock(injector, AggregationServiceConfiguration.class);
        setUpInjectorMock(injector, DevelopmentConfigurationSeeder.class);
        CryptoConfigurationDao dao = setUpInjectorMock(injector, CryptoConfigurationDao.class);

        ConfigurationValidator validator =
                new ConfigurationValidator(new HashMap<>(), new HashMap<>(), new HashMap<>(), dao);
        when(injector.getInstance(ConfigurationValidator.class)).thenReturn(validator);

        setUpInjectorMock(injector, DrainModeTask.class);

        managedTppSecretsServiceClient =
                setUpInjectorMock(injector, ManagedTppSecretsServiceClient.class);
        agentWorker = setUpInjectorMock(injector, AgentWorker.class);
        setUpInjectorMock(injector, AgentDataAvailabilityTrackerClient.class);

        AggregationServiceContainer actualAggregationContainer = new AggregationServiceContainer();
        aggregationContainer = Mockito.spy(actualAggregationContainer);
        doReturn(injector).when(aggregationContainer).generateInjector(any(), any());
    }

    @Test
    public void ensureLifecycleOfSSClientAndAgentWorker() throws Exception {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        MetricRegistry metricRegistry = new MetricRegistry();

        Environment environment =
                new Environment(
                        "test",
                        objectMapper,
                        validator,
                        metricRegistry,
                        ClassLoader.getSystemClassLoader());
        aggregationContainer.run(configuration, environment);
        final Server server = serverFactory.build(environment);
        server.setHandler(
                new AbstractHandler() {
                    @Override
                    public void handle(
                            String s,
                            Request request,
                            HttpServletRequest httpServletRequest,
                            HttpServletResponse httpServletResponse)
                            throws IOException, ServletException {
                        // This is empty since the server does not have to be functioning
                        // It just has to be able to start and stop
                    }
                });
        server.start();
        InOrder startOrder = inOrder(managedTppSecretsServiceClient, agentWorker);
        startOrder.verify(managedTppSecretsServiceClient).start();
        startOrder.verify(agentWorker).start();

        server.stop();
        InOrder stopOrder = inOrder(agentWorker, managedTppSecretsServiceClient);
        stopOrder.verify(agentWorker).stop();
        stopOrder.verify(managedTppSecretsServiceClient).stop();
    }

    private <T> T setUpInjectorMock(Injector injector, Class<T> type) {
        T mocked = mock(type);
        when(injector.getInstance(type)).thenReturn(mocked);
        return mocked;
    }
}
