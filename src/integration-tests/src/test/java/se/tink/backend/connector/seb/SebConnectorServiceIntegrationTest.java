package se.tink.backend.connector.seb;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import java.util.List;
import org.junit.BeforeClass;
import org.mockito.Mockito;
import se.tink.backend.client.ServiceFactory;
import se.tink.backend.combined.ServiceIntegrationTest;
import se.tink.backend.common.tasks.interfaces.TaskSubmitter;
import se.tink.backend.connector.configuration.ConnectorModulesFactory;
import se.tink.backend.system.client.SystemServiceFactory;

/**
 * TODO this is a unit test
 */
public class SebConnectorServiceIntegrationTest extends ServiceIntegrationTest {

    private static final String CONFIG_FILE_PATH = "etc/seb/development-connector-server.yml";
    private static final int PORT = 8080;

    private static class MockModule extends AbstractModule {
        public void configure() {
            bind(TaskSubmitter.class).toInstance(Mockito.mock(TaskSubmitter.class));
        }
    }

    @BeforeClass
    public static void beforeClassSetup() throws Exception {
        List<String> services = Lists.newArrayList(SystemServiceFactory.SERVICE_NAME, ServiceFactory.SERVICE_NAME);
        preSetup(CONFIG_FILE_PATH, PORT, services);
        injector = Guice.createInjector(
                Modules.override(ConnectorModulesFactory.build(configuration, Mockito.mock(JerseyEnvironment.class)))
                        .with(new MockModule()));
    }
}
