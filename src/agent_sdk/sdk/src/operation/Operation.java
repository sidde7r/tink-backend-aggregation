package se.tink.agent.sdk.operation;

import se.tink.agent.sdk.storage.Storage;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;

public interface Operation {

    Storage getAgentStorage();

    Provider getProvider();

    StaticBankCredentials getStaticBankCredentials();

    AgentConfigurationControllerable getAggregatorConfiguration();
}
