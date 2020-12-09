package se.tink.backend.integration.agent_data_availability_tracker.client;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.IdentityData;
import se.tink.backend.integration.agent_data_availability_tracker.serialization.AccountTrackingSerializer;
import se.tink.backend.integration.agent_data_availability_tracker.serialization.IdentityDataSerializer;

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
    public void sendIdentityData(
            final String agent,
            final String provider,
            final String market,
            final se.tink.backend.aggregation.aggregationcontroller.v1.rpc.IdentityData
                    identityData) {}

    @Override
    public AccountTrackingSerializer serializeAccount(
            final Account account, final AccountFeatures features) {
        return new AccountTrackingSerializer(new Account());
    }

    @Override
    public IdentityDataSerializer serializeIdentityData(final IdentityData identityData) {
        return new IdentityDataSerializer(new IdentityData());
    }

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
