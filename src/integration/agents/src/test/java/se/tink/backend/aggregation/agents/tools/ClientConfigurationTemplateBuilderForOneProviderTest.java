package se.tink.backend.aggregation.agents.tools;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class ClientConfigurationTemplateBuilderForOneProviderTest {

    private ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());
    private ClientConfigurationTemplateBuilderTest clientConfigurationTemplateBuilderTest;

    @Before
    public void setup() {
        manager.before();
        clientConfigurationTemplateBuilderTest =
                new ClientConfigurationTemplateBuilderTest.Builder(
                                manager.get(Arg.MARKET), manager.get(Arg.PROVIDER_NAME))
                        .build();
    }

    @Test
    public void testTemplateBuilding() {
        System.out.println(
                clientConfigurationTemplateBuilderTest
                        .getClientConfigurationTemplateBuilder()
                        .buildTemplate());
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    private enum Arg {
        MARKET,
        PROVIDER_NAME
    }
}
