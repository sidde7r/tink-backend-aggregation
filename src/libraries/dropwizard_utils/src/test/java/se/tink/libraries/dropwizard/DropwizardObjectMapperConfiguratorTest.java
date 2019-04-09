package se.tink.libraries.dropwizard;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.sun.jersey.api.container.ContainerException;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import org.junit.Test;

public class DropwizardObjectMapperConfiguratorTest {
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    static class TestConfiguration extends Configuration {
        @SuppressWarnings("unused")
        private String testProperty;
    }

    static class TestApplication extends Application<TestConfiguration> {
        @Override
        public void initialize(Bootstrap<TestConfiguration> bootstrap) {
            DropwizardObjectMapperConfigurator.doNotFailOnUnknownProperties(bootstrap);

            String yamlConfiguration =
                    "testProperty: testPropertyValue\n"
                            + "unrecognizedProperty: unrecognizedPropertyValue";
            bootstrap.setConfigurationSourceProvider(
                    path ->
                            new ByteArrayInputStream(
                                    yamlConfiguration.getBytes(StandardCharsets.UTF_8)));
        }

        @Override
        public void run(TestConfiguration configuration, Environment environment) {
            assertEquals(
                    "configuration property should be mapped",
                    "testPropertyValue",
                    configuration.testProperty);
        }
    }

    @Test
    public void doNotFailOnUnknownProperties() throws Exception {
        try {
            new TestApplication().run(new String[] {"server", "config-path"});
        } catch (ServletException expected) {
            assertEquals(
                    "TestApplication is expected to fail as it doesn't have any resources configured",
                    ContainerException.class,
                    expected.getCause().getClass());
        }
    }
}
