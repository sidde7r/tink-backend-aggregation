package se.tink.backend.aggregation.agents.agentcapabilities;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Test;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;

public class CapabilitiesExtractorTest {

    @Test
    public void shouldExtractCapabilitiesFromImplementedExecutors() {
        Set<Capability> capabilities =
                CapabilitiesExtractor.readCapabilities(TestAgentImplementingExecutors.class);
        assertThat(capabilities).isEqualTo(newHashSet(Capability.LOANS, Capability.IDENTITY_DATA));
    }

    @Test
    public void shouldExtractCapabilitiesFromAnnotation() {
        Set<Capability> capabilities =
                CapabilitiesExtractor.readCapabilities(TestAgentWithListedCapabilities.class);
        assertThat(capabilities)
                .isEqualTo(newHashSet(Capability.CHECKING_ACCOUNTS, Capability.SAVINGS_ACCOUNTS));
    }
}
