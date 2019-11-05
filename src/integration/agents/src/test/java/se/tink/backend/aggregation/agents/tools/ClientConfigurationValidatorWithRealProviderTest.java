package se.tink.backend.aggregation.agents.tools;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class ClientConfigurationValidatorWithRealProviderTest {
    private ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());
    private ClientConfigurationValidatorBuilderForTest clientConfigurationValidatorBuilderForTest;

    @Before
    public void setup() {
        manager.before();
        clientConfigurationValidatorBuilderForTest =
                new ClientConfigurationValidatorBuilderForTest.Builder(
                                manager.get(Arg.MARKET), manager.get(Arg.PROVIDER_NAME))
                        .build();
    }

    @Test
    public void ClientConfigurationValidationTest() {}

    private enum Arg {
        MARKET,
        PROVIDER_NAME
    }
}
