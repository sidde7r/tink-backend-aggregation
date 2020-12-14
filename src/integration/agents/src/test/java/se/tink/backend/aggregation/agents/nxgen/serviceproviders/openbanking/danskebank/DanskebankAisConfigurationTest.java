package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.enums.MarketCode;

public class DanskebankAisConfigurationTest {

    private static final String API_BASE_URL = "/api/base/url";
    private static final String WELL_KNOWN_URL = "/well/known/url";
    private static final String IDENTITY_DATA_URL = "/identity/data/url";

    private DanskebankAisConfiguration.Builder builder;

    @Before
    public void setUp() {
        builder = new DanskebankAisConfiguration.Builder(API_BASE_URL, MarketCode.DK);
    }

    @Test
    public void buildConfiguration() {
        // given
        builder.withWellKnownURL(new URL(WELL_KNOWN_URL))
                .withIdentityDataURL(IDENTITY_DATA_URL)
                .withAdditionalPermission("additional permission 1")
                .withAdditionalPermission("additional permission 2")
                .partyEndpointEnabled(false);

        // when
        DanskebankAisConfiguration result = builder.build();

        // then
        assertThat(result.isPartyEndpointEnabled()).isFalse();
        assertThat(result.getWellKnownURL()).isEqualTo(new URL(WELL_KNOWN_URL));
        assertThat(result.getIdentityDataURL()).isEqualTo(new URL(IDENTITY_DATA_URL));
        assertThat(result.isAccountPartiesEndpointEnabled()).isEqualTo(false);
        assertThat(result.isAccountPartyEndpointEnabled()).isEqualTo(false);
        assertThat(result.getAdditionalPermissions())
                .containsOnly("additional permission 1", "additional permission 2");
        assertThat(result.getApiBaseURL()).isEqualTo(new URL(API_BASE_URL));
    }

    @Test
    public void partyEndpointEnabledByDefault() {
        // given

        // when
        DanskebankAisConfiguration result = builder.build();

        // then
        assertThat(result.isPartyEndpointEnabled()).isTrue();
    }
}
