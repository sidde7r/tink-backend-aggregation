package se.tink.backend.integration.agent_data_availability_tracker.client;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;

public interface AgentDataAvailabilityTrackerClient {

    void beginStream();
    void sendAccount(final String agent, final Account account, final AccountFeatures features);
    void endStreamBlocking();
}
