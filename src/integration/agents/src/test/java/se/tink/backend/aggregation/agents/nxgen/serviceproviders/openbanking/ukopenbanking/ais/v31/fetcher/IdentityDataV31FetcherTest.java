package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UKOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.IdentityDataMapper;
import se.tink.libraries.identitydata.IdentityData;

public class IdentityDataV31FetcherTest {

    private IdentityDataV31Fetcher identityDataV31Fetcher;
    private UKOpenBankingAis aisConfiguration;
    private UkOpenBankingApiClient apiClient;
    private IdentityDataMapper identityDataMapper;

    @Before
    public void setUp() {
        apiClient = mock(UkOpenBankingApiClient.class);
        aisConfiguration = mock(UKOpenBankingAis.class);
        identityDataMapper = mock(IdentityDataMapper.class);
        identityDataV31Fetcher =
                new IdentityDataV31Fetcher(apiClient, aisConfiguration, identityDataMapper);
    }

    @Test
    public void doNotfetchPartyOnlyIfItsNotEnabledInConfig() {
        // when
        when(aisConfiguration.isPartyEndpointEnabled()).thenReturn(false);
        Optional<IdentityData> result = identityDataV31Fetcher.fetchIdentityData();

        // then
        verifyZeroInteractions(apiClient);
        assertThat(result).isEmpty();
    }

    @Test
    public void fetchIdentityDataReturnsCorrectResult() {
        // given
        IdentityData expectedResponse =
                IdentityData.builder()
                        .setFullName("Elon Musk")
                        .setDateOfBirth(LocalDate.parse("2019-01-01"))
                        .build();

        // when
        when(aisConfiguration.isPartyEndpointEnabled()).thenReturn(true);
        when(apiClient.fetchV31Party()).thenReturn(Optional.of(mock(IdentityDataV31Entity.class)));
        when(identityDataMapper.map(any())).thenReturn(expectedResponse);

        Optional<IdentityData> result = identityDataV31Fetcher.fetchIdentityData();
        // then
        assertThat(result.get().getDateOfBirth()).isEqualTo(expectedResponse.getDateOfBirth());
        assertThat(result.get()).isEqualTo(expectedResponse);
    }
}
