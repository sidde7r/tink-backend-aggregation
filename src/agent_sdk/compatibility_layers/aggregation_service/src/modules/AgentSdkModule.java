package src.agent_sdk.compatibility_layers.aggregation_service.src.modules;

import com.google.inject.AbstractModule;
import se.tink.agent.runtime.utils.RandomGeneratorImpl;
import se.tink.agent.runtime.utils.SleeperImpl;
import se.tink.agent.runtime.utils.TimeGeneratorImpl;
import se.tink.agent.sdk.utils.RandomGenerator;
import se.tink.agent.sdk.utils.Sleeper;
import se.tink.agent.sdk.utils.TimeGenerator;

public class AgentSdkModule extends AbstractModule {
    private final RandomGenerator randomGenerator;
    private final Sleeper sleeper;
    private final TimeGenerator timeGenerator;

    public AgentSdkModule() {
        this.randomGenerator = new RandomGeneratorImpl();
        this.sleeper = new SleeperImpl();
        this.timeGenerator = new TimeGeneratorImpl();
    }

    @Override
    protected void configure() {
        install(new UserProviderModule());
        install(new StaticBankCredentialsProviderModule());
        install(new ProviderProviderModule());
        install(new SupplementalInformationHelperProviderModule());
        install(new AggregatorConfigurationProviderModule());
        install(new RawAgentStorageProviderModule());
        install(new OperationProviderModule());
        install(new UtilitiesProviderModule());
        bind(RandomGenerator.class).toInstance(this.randomGenerator);
        bind(Sleeper.class).toInstance(this.sleeper);
        bind(TimeGenerator.class).toInstance(this.timeGenerator);
    }
}
