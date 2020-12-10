package se.tink.backend.integration.agent_data_availability_tracker.client;

import se.tink.backend.integration.agent_data_availability_tracker.common.client.AgentDataAvailabilityTrackerClient;
import se.tink.libraries.dropwizard_lifecycle.ManagedSafeStop;

public abstract class AsAgentDataAvailabilityTrackerClient extends ManagedSafeStop
        implements AgentDataAvailabilityTrackerClient {}
