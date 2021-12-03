package se.tink.agent.sdk.environment;

import se.tink.agent.sdk.operation.Provider;
import se.tink.agent.sdk.operation.StaticBankCredentials;
import se.tink.agent.sdk.storage.Storage;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;

public interface Operation {

    Storage getAgentStorage();

    Provider getProvider();

    StaticBankCredentials getStaticBankCredentials();

    AgentConfigurationControllerable getAggregatorConfiguration();
}
