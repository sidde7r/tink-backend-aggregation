package src.agent_sdk.compatibility_layers.aggregation_service.test.modules;

import com.google.inject.AbstractModule;
import se.tink.agent.runtime.test.utils.FakeRandomGeneratorImpl;
import se.tink.agent.runtime.test.utils.FakeSleeperImpl;
import se.tink.agent.runtime.test.utils.FakeTimeGeneratorImpl;
import se.tink.agent.sdk.utils.RandomGenerator;
import se.tink.agent.sdk.utils.Sleeper;
import se.tink.agent.sdk.utils.TimeGenerator;
import src.agent_sdk.compatibility_layers.aggregation_service.src.modules.AggregatorConfigurationProviderModule;
import src.agent_sdk.compatibility_layers.aggregation_service.src.modules.OperationProviderModule;
import src.agent_sdk.compatibility_layers.aggregation_service.src.modules.ProviderProviderModule;
import src.agent_sdk.compatibility_layers.aggregation_service.src.modules.RawAgentStorageProviderModule;
import src.agent_sdk.compatibility_layers.aggregation_service.src.modules.StaticBankCredentialsProviderModule;
import src.agent_sdk.compatibility_layers.aggregation_service.src.modules.SupplementalInformationHelperProviderModule;
import src.agent_sdk.compatibility_layers.aggregation_service.src.modules.UserProviderModule;

public class AgentSdkTestModule extends AbstractModule {
    private final RandomGenerator randomGenerator;
    private final Sleeper sleeper;
    private final TimeGenerator timeGenerator;

    public AgentSdkTestModule() {
        this.randomGenerator = new FakeRandomGeneratorImpl();
        this.sleeper = new FakeSleeperImpl();
        this.timeGenerator = new FakeTimeGeneratorImpl();
    }

    @Override
    protected void configure() {
        // Remove install from AgentWireMockComponentProviderModule when AgentSdkTestModule is
        // added to AgentWireMockComponentProviderModule
        // ---------------------------------------------------------------------------------
        install(new UserProviderModule());
        install(new StaticBankCredentialsProviderModule());
        install(new ProviderProviderModule());
        // ---------------------------------------------------------------------------------

        install(new SupplementalInformationHelperProviderModule());
        install(new AggregatorConfigurationProviderModule());
        install(new RawAgentStorageProviderModule());
        install(new OperationProviderModule());
        bind(RandomGenerator.class).toInstance(this.randomGenerator);
        bind(Sleeper.class).toInstance(this.sleeper);
        bind(TimeGenerator.class).toInstance(this.timeGenerator);
    }
}
