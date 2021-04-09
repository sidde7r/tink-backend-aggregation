package se.tink.backend.aggregation.agents.agentcapabilities;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;
import se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability;
import se.tink.libraries.enums.MarketCode;

public class CapabilitiesExtractorTest {

    private static final String MARKET = "GB";

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

    @Test
    public void shouldExtractPisCapabilitiesFromAnnotation() {
        final Map<String, Set<String>> pisCapabilities =
                CapabilitiesExtractor.readPisCapabilities(TestAgentWithListedPisCapabilities.class);

        Arrays.stream(MarketCode.values())
                .map(MarketCode::name)
                .forEach(
                        marketCode -> {
                            assertThat(pisCapabilities.containsKey(marketCode)).isTrue();
                            assertThat(pisCapabilities.get(marketCode))
                                    .containsExactlyInAnyOrder(
                                            PisCapability.PIS_SEPA.name(),
                                            PisCapability.PIS_SEPA_ICT.name());
                        });
    }

    @Test
    public void shouldExtractMarketSpecificPisCapabilitiesFromAnnotation() {
        final Map<String, Set<String>> pisCapabilities =
                CapabilitiesExtractor.readPisCapabilities(
                        TestAgentWithListedMarketSpecificPisCapabilities.class);

        assertThat(pisCapabilities).hasSize(1);
        assertThat(pisCapabilities.containsKey(MARKET)).isTrue();
        assertThat(pisCapabilities.get(MARKET))
                .containsOnly(PisCapability.PIS_UK_FASTER_PAYMENT.name());
    }

    @Test
    public void shouldExtractRepeatedMarketSpecificPisCapabilitiesFromMultipleAnnotations() {
        final Map<String, Set<String>> pisCapabilities =
                CapabilitiesExtractor.readPisCapabilities(
                        TestAgentWithListedPisCapabilitiesWithRepeatedMarket.class);
        assertThat(pisCapabilities).hasSize(1);
        assertThat(pisCapabilities.containsKey(MARKET)).isTrue();
        assertThat(pisCapabilities.get(MARKET))
                .containsExactlyInAnyOrder(
                        PisCapability.PIS_SEPA.name(), PisCapability.PIS_SEPA_ICT.name());
    }
}
