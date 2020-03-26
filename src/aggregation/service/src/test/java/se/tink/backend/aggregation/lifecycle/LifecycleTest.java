package se.tink.backend.aggregation;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import javax.validation.Validation;
import javax.validation.Validator;
import org.eclipse.jetty.server.Server;
import org.junit.Before;
import org.junit.Test;
public class LifecycleTest {
    private DefaultServerFactory http;
    final ObjectMapper objectMapper = Jackson.newObjectMapper();
    @Before
    public void setUp() throws IOException, ConfigurationException {
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory .class,
                FileAppenderFactory.class,
                SyslogAppenderFactory.class,
                HttpConnectorFactory.class);
        this.http = new ConfigurationFactory<>(DefaultServerFactory.class,
                Validation.buildDefaultValidatorFactory().getValidator(),
                objectMapper, "dw").build();
    }
    @Test
    public void test() {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        MetricRegistry metricRegistry = new MetricRegistry();
        Environment environment = new Environment("test", objectMapper, validator, metricRegistry,
                ClassLoader.getSystemClassLoader());
        final Server server = http.build(environment);
    }
}