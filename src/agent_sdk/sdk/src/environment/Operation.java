package se.tink.agent.sdk.environment;

import se.tink.agent.sdk.operation.Provider;
import se.tink.agent.sdk.operation.StaticBankCredentials;
import se.tink.agent.sdk.operation.aggregator_configuration.AggregatorConfiguration;
import se.tink.agent.sdk.storage.Storage;

public interface Operation {
    Storage getAgentStorage();

    Provider getProvider();

    StaticBankCredentials getStaticBankCredentials();

    AggregatorConfiguration getAggregatorConfiguration();
}
