package se.tink.backend.aggregation.eidasidentity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.libraries.unleash.UnleashClient;

@RunWith(MockitoJUnitRunner.class)
public class CertificateIdProviderTest {
    private static final String APP_ID = "appId";
    private static final String CLUSTER_ID = "clusterId";
    private static final String PROVIDER_NAME = "providerName";
    private static final String MARKET_CODE = "IT";
    private static final String UK_MARKET_CODE = "UK";

    @Mock private UnleashClient unleashClient;
    private CertificateIdProvider certificateIdentityService;

    @Before
    public void setUp() throws Exception {
        this.certificateIdentityService = new UnleashCertificateIdProvider(unleashClient);
    }

    @Test
    public void shouldReturnUKOBCertIdWhenMarketCodeIsUK() {
        // when
        String result =
                certificateIdentityService.getCertId(
                        APP_ID, CLUSTER_ID, PROVIDER_NAME, UK_MARKET_CODE, true);

        // then
        assertThat(result).isEqualTo("UKOB");
    }

    @Test
    public void shouldReturnUKOBCertIdWhenMarketCodeIsUKAppIdAndProviderNameAreNotAllowed() {
        // given
        when(unleashClient.isToggleEnabled(any())).thenReturn(false);

        // when
        String result =
                certificateIdentityService.getCertId(
                        APP_ID, CLUSTER_ID, PROVIDER_NAME, UK_MARKET_CODE, true);

        // then
        assertThat(result).isEqualTo("UKOB");
    }

    @Test
    public void shouldReturnOLD_EIDASCertIdWhenMarketCodeIsUKAndAppIdAndProviderNameAreAllowed() {
        // given
        when(unleashClient.isToggleEnabled(any())).thenReturn(true);

        // when
        String result =
                certificateIdentityService.getCertId(
                        APP_ID, CLUSTER_ID, PROVIDER_NAME, UK_MARKET_CODE, true);

        // then
        assertThat(result).isEqualTo("OLD_EIDAS");
    }

    @Test
    public void shouldReturnOLD_EIDASCertIdWhenAppIdAndProviderNameAreAllowed() {
        // given
        when(unleashClient.isToggleEnabled(any())).thenReturn(true);

        // when
        String result =
                certificateIdentityService.getCertId(
                        APP_ID, CLUSTER_ID, PROVIDER_NAME, MARKET_CODE, true);

        // then
        assertThat(result).isEqualTo("OLD_EIDAS");
    }

    @Test
    public void shouldReturnDEFAULTCertIdWhenAppIdAndProviderNameAreNotAllowed() {
        // given
        when(unleashClient.isToggleEnabled(any())).thenReturn(false);

        // when
        String result =
                certificateIdentityService.getCertId(
                        APP_ID, CLUSTER_ID, PROVIDER_NAME, MARKET_CODE, true);

        // then
        assertThat(result).isEqualTo("DEFAULT");
    }

    @Test
    public void shouldReturnDEFAULTCertIdWhenProviderIsNotOpenBanking() {
        // when
        String result =
                certificateIdentityService.getCertId(
                        APP_ID, CLUSTER_ID, PROVIDER_NAME, MARKET_CODE, false);

        // then
        assertThat(result).isEqualTo("DEFAULT");
    }
}
