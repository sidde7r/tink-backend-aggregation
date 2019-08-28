package se.tink.backend.integration.agent_data_availability_tracker.client;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;

public class AgentDataAvailabilityTrackerMockClientImpl
        implements AgentDataAvailabilityTrackerClient {

    @Override
    public void beginStream() {
        // Noop
    }

    @Override
    public void sendAccount(String agent, Account account, AccountFeatures features) {
        // Noop
    }

    @Override
    public void endStreamBlocking() {
        // Noop
    }

    @Override
    public boolean sendingRealData() {
        return false;
    }

    @Override
    public void start() throws Exception {}

    @Override
    public void stop() throws Exception {}
}
