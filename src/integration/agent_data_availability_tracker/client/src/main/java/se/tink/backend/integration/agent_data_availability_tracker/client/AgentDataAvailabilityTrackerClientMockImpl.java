package se.tink.backend.integration.agent_data_availability_tracker.client;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;

public class AgentDataAvailabilityTrackerClientMockImpl
        implements AgentDataAvailabilityTrackerClient {

    @Override
    public void beginStream() {}

    @Override
    public void sendAccount(
            String agent,
            String provider,
            String market,
            Account account,
            AccountFeatures features) {}

    @Override
    public void endStreamBlocking() throws InterruptedException {}

    @Override
    public boolean sendingRealData() {
        return false;
    }

    @Override
    public void start() throws Exception {}

    @Override
    public void stop() throws Exception {}
}
