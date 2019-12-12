package se.tink.backend.aggregation.agents.tools;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;

public class ClientConfigurationTemplateBuilderForOneProviderTest {

    private ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());
    private ClientConfigurationTemplateBuilderTest clientConfigurationTemplateBuilderTest;

    @Before
    public void setup() {
        manager.before();
        clientConfigurationTemplateBuilderTest =
                new ClientConfigurationTemplateBuilderTest.Builder(
                                manager.get(Arg.MARKET),
                                manager.get(Arg.PROVIDER_NAME),
                                Boolean.parseBoolean(manager.get(Arg.INCLUDE_DESCRIPTIONS)),
                                Boolean.parseBoolean(manager.get(Arg.INCLUDE_EXAMPLES)))
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

    private enum Arg implements ArgumentManagerEnum {
        MARKET,
        PROVIDER_NAME,
        INCLUDE_DESCRIPTIONS,
        INCLUDE_EXAMPLES;

        private final boolean optional;

        Arg(boolean optional) {
            this.optional = optional;
        }

        Arg() {
            this.optional = false;
        }

        @Override
        public boolean isOptional() {
            return optional;
        }
    }
}
