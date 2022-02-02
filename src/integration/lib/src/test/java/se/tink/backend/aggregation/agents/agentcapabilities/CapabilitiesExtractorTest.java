package se.tink.backend.aggregation.agents.agentcapabilities;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import se.tink.libraries.enums.MarketCode;

public class CapabilitiesExtractorTest {

    private static final String MARKET = "GB";
    private static final MarketCode MARKET_AS_CODE = MarketCode.GB;

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
    public void shouldExtractPisCapabilitiesAsStringsFromAnnotation() {
        final Map<String, Set<String>> pisCapabilities =
                CapabilitiesExtractor.readPisCapabilitiesAsStrings(
                        TestAgentWithListedPisCapabilities.class);

        Arrays.stream(MarketCode.values())
                .map(MarketCode::name)
                .forEach(
                        marketCode -> {
                            assertThat(pisCapabilities.containsKey(marketCode)).isTrue();
                            assertThat(pisCapabilities.get(marketCode))
                                    .containsExactlyInAnyOrder(
                                            PisCapability.SEPA_CREDIT_TRANSFER.name(),
                                            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER.name());
                        });
    }

    @Test
    public void shouldExtractMarketSpecificPisCapabilitiesAsStringsFromAnnotation() {
        final Map<String, Set<String>> pisCapabilities =
                CapabilitiesExtractor.readPisCapabilitiesAsStrings(
                        TestAgentWithListedMarketSpecificPisCapabilities.class);

        assertThat(pisCapabilities).hasSize(1);
        assertThat(pisCapabilities.containsKey(MARKET)).isTrue();
        assertThat(pisCapabilities.get(MARKET)).containsOnly(PisCapability.FASTER_PAYMENTS.name());
    }

    @Test
    public void
            shouldExtractRepeatedMarketSpecificPisCapabilitiesAsStringsFromMultipleAnnotations() {
        final Map<String, Set<String>> pisCapabilities =
                CapabilitiesExtractor.readPisCapabilitiesAsStrings(
                        TestAgentWithListedPisCapabilitiesWithRepeatedMarket.class);
        assertThat(pisCapabilities).hasSize(1);
        assertThat(pisCapabilities.containsKey(MARKET)).isTrue();
        assertThat(pisCapabilities.get(MARKET))
                .containsExactlyInAnyOrder(
                        PisCapability.SEPA_CREDIT_TRANSFER.name(),
                        PisCapability.SEPA_INSTANT_CREDIT_TRANSFER.name());
    }

    @Test
    public void shouldExtractPisCapabilitiesFromAnnotation() {
        final Map<MarketCode, Set<PisCapability>> pisCapabilities =
                CapabilitiesExtractor.readPisCapabilities(TestAgentWithListedPisCapabilities.class);

        Arrays.stream(MarketCode.values())
                .forEach(
                        marketCode -> {
                            assertThat(pisCapabilities.containsKey(marketCode)).isTrue();
                            assertThat(pisCapabilities.get(marketCode))
                                    .containsExactlyInAnyOrder(
                                            PisCapability.SEPA_CREDIT_TRANSFER,
                                            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER);
                        });
    }

    @Test
    public void shouldExtractMarketSpecificPisCapabilitiesFromAnnotation() {
        final Map<MarketCode, Set<PisCapability>> pisCapabilities =
                CapabilitiesExtractor.readPisCapabilities(
                        TestAgentWithListedMarketSpecificPisCapabilities.class);

        assertThat(pisCapabilities).hasSize(1);
        assertThat(pisCapabilities.containsKey(MARKET_AS_CODE)).isTrue();
    }

    @Test
    public void shouldExtractRepeatedMarketSpecificPisCapabilitiesFromMultipleAnnotations() {
        final Map<MarketCode, Set<PisCapability>> pisCapabilities =
                CapabilitiesExtractor.readPisCapabilities(
                        TestAgentWithListedPisCapabilitiesWithRepeatedMarket.class);
        assertThat(pisCapabilities).hasSize(1);
        assertThat(pisCapabilities.containsKey(MARKET_AS_CODE)).isTrue();
        assertThat(pisCapabilities.get(MARKET_AS_CODE))
                .containsExactlyInAnyOrder(
                        PisCapability.SEPA_CREDIT_TRANSFER,
                        PisCapability.SEPA_INSTANT_CREDIT_TRANSFER);
    }
}
