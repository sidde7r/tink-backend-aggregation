package se.tink.backend.integration.agent_data_availability_tracker.client;

import com.google.common.base.Strings;

public class AgentDataAvailabilityTrackerClientFactory {

    private static final String TEST_PROVIDER = "tracking-test-provider";

    public static AgentDataAvailabilityTrackerClient getClient(
            final AgentDataAvailabilityTrackerConfiguration configuration,
            final String providerName) {

        if (configuration != null && !Strings.isNullOrEmpty(configuration.getHost())) {

            /*
             *  Temporary limitation to prevent client running on all providers.
             */
            if (TEST_PROVIDER.equalsIgnoreCase(providerName)) {
                return new AgentDataAvailabilityTrackerClientImpl(
                        configuration.getHost(), configuration.getPort());
            }
        }

        // Default to noop mock client
        return new AgentDataAvailabilityTrackerMockClientImlp();
    }
}
