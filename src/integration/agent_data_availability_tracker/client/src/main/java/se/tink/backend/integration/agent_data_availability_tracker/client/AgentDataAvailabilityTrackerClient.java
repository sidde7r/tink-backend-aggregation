package se.tink.backend.integration.agent_data_availability_tracker.client;

import io.dropwizard.lifecycle.Managed;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.IdentityData;
import se.tink.backend.integration.agent_data_availability_tracker.serialization.AccountTrackingSerializer;
import se.tink.backend.integration.agent_data_availability_tracker.serialization.IdentityDataSerializer;

public interface AgentDataAvailabilityTrackerClient extends Managed {

    void beginStream();

    void sendAccount(
            final String agent,
            final String provider,
            final String market,
            final Account account,
            final AccountFeatures features);

    AccountTrackingSerializer serializeAccount(
            final Account account, final AccountFeatures features);

    IdentityDataSerializer serializeIdentityData(final IdentityData identityData);

    void sendIdentityData(
            final String agent,
            final String provider,
            final String market,
            final se.tink.backend.aggregation.aggregationcontroller.v1.rpc.IdentityData
                    identityData);

    void endStreamBlocking() throws InterruptedException;

    boolean sendingRealData();
}
