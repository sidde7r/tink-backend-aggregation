package se.tink.backend.integration.agent_data_availability_tracker.module;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;
import se.tink.backend.integration.agent_data_availability_tracker.client.AsAgentDataAvailabilityTrackerClient;
import se.tink.backend.integration.agent_data_availability_tracker.client.AsAgentDataAvailabilityTrackerClientImpl;
import se.tink.backend.integration.agent_data_availability_tracker.client.AsAgentDataAvailabilityTrackerClientMockImpl;
import se.tink.backend.integration.agent_data_availability_tracker.common.configuration.AgentDataAvailabilityTrackerConfiguration;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class AgentDataAvailabilityTrackerModuleTest {

    @Test
    public void constructingMockImplWorks() {
        AgentDataAvailabilityTrackerConfiguration configuration =
                new AgentDataAvailabilityTrackerConfiguration(null, 0, null);
        Injector injector =
                Guice.createInjector(new AgentDataAvailabilityTrackerModule(configuration));

        AsAgentDataAvailabilityTrackerClient client =
                injector.getInstance(AsAgentDataAvailabilityTrackerClient.class);

        assertThat(client, instanceOf(AsAgentDataAvailabilityTrackerClientMockImpl.class));
    }

    @Test
    public void constructingImplWorks() {
        String json =
                "{\"host\":\"127.0.0.1\", \"port\": 8080, \"caPath\":\"data/test/qsealc/ca.crt\"}";
        AgentDataAvailabilityTrackerConfiguration configuration =
                SerializationUtils.deserializeFromString(
                        json, AgentDataAvailabilityTrackerConfiguration.class);
        Injector injector =
                Guice.createInjector(new AgentDataAvailabilityTrackerModule(configuration));

        AsAgentDataAvailabilityTrackerClient client =
                injector.getInstance(AsAgentDataAvailabilityTrackerClient.class);

        assertThat(client, instanceOf(AsAgentDataAvailabilityTrackerClientImpl.class));
    }
}
