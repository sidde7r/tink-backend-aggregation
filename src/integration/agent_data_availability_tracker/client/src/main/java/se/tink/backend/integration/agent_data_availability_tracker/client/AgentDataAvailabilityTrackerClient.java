package se.tink.backend.integration.agent_data_availability_tracker.client;

import io.dropwizard.lifecycle.Managed;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;

public interface AgentDataAvailabilityTrackerClient extends Managed {

    void beginStream();

    void sendAccount(
            final String agent,
            final String provider,
            final String market,
            final Account account,
            final AccountFeatures features);

    void endStreamBlocking() throws InterruptedException;

    boolean sendingRealData();
}
