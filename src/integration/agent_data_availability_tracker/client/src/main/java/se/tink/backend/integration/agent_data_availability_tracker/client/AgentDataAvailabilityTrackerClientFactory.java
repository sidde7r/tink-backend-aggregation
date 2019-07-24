package se.tink.backend.integration.agent_data_availability_tracker.client;

import com.google.common.base.Strings;
import javax.net.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentDataAvailabilityTrackerClientFactory {
    private static final Logger log =
            LoggerFactory.getLogger(AgentDataAvailabilityTrackerClientFactory.class);

    public static AgentDataAvailabilityTrackerClient getClient(
            final AgentDataAvailabilityTrackerConfiguration configuration,
            final boolean forceMockClient) {
        if (configuration != null && !Strings.isNullOrEmpty(configuration.getHost())) {

            /*
             *  Temporary limitation to prevent client running on all providers.
             */
            if (!forceMockClient) {
                try {
                    return new AgentDataAvailabilityTrackerClientImpl(
                            configuration.getHost(), configuration.getPort());
                } catch (SSLException e) {

                    log.error("Could not instantiate client.", e);
                }
            }
        }

        // Default to noop mock client
        return new AgentDataAvailabilityTrackerMockClientImpl();
    }
}
