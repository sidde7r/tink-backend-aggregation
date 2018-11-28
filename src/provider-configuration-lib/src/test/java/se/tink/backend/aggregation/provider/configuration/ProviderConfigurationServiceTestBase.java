package se.tink.backend.aggregation.provider.configuration;

import com.google.inject.Injector;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class ProviderConfigurationServiceTestBase {
    protected static Injector injector;

    @BeforeClass
    public static void init() {
        injector = InjectorFactory.get("etc/development-provider-configuration-server.yml");
    }

    @Before
    public void setup() {
        injector.injectMembers(this);
    }
}
