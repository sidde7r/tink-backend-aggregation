package se.tink.agent.sdk.environment;

import se.tink.agent.sdk.operation.Provider;
import se.tink.agent.sdk.operation.StaticBankCredentials;
import se.tink.agent.sdk.operation.User;
import se.tink.agent.sdk.operation.aggregator_configuration.AggregatorConfiguration;
import se.tink.agent.sdk.storage.Storage;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;

public interface Operation {
    Storage getAgentStorage();

    User getUser();

    Provider getProvider();

    StaticBankCredentials getStaticBankCredentials();

    AggregatorConfiguration getAggregatorConfiguration();

    EidasProxyConfiguration getEidasProxyConfiguration();
}
