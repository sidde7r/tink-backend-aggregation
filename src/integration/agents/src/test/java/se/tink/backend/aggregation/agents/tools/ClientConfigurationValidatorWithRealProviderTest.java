package se.tink.backend.aggregation.agents.tools;

import org.junit.Before;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

public class ClientConfigurationValidatorTest {
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



    private enum Arg {
        MARKET,
        PROVIDER_NAME
    }
}
