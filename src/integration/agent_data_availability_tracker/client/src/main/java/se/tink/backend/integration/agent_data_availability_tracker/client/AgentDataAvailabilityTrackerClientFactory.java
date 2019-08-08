package se.tink.backend.integration.agent_data_availability_tracker.client;

import com.google.common.base.Strings;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentDataAvailabilityTrackerClientFactory {
    private static final Logger log =
            LoggerFactory.getLogger(AgentDataAvailabilityTrackerClientFactory.class);

    private static final float TRACKING_FRACTION = 0.10f; // 10% of requests
    private static AgentDataAvailabilityTrackerClientFactory instance;

    private final Random random;

    private AgentDataAvailabilityTrackerClientFactory() {
        random = new Random();
    }

    public static AgentDataAvailabilityTrackerClientFactory getInstance() {

        if (instance == null) {
            instance = new AgentDataAvailabilityTrackerClientFactory();
        }

        return instance;
    }

    public AgentDataAvailabilityTrackerClient getClient(
            final AgentDataAvailabilityTrackerConfiguration configuration,
            final boolean forceMockClient) {
        if (configuration != null && !Strings.isNullOrEmpty(configuration.getHost())) {

            /*
             *  Temporary limitation to prevent client running on all providers.
             */
            if (!forceMockClient && shouldTrackData()) {
                try {
                    return new AgentDataAvailabilityTrackerClientImpl(
                            configuration.getHost(), configuration.getPort());
                } catch (Exception e) {

                    log.error("Could not instantiate client.", e);
                }
            }
        }

        // Default to noop mock client
        return new AgentDataAvailabilityTrackerMockClientImpl();
    }

    private boolean shouldTrackData() {
        return random.nextFloat() < TRACKING_FRACTION;
    }
}
