package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UKOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.PartyFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.IdentityDataMapper;
import se.tink.libraries.identitydata.IdentityData;

public class IdentityDataV31FetcherTest {

    private IdentityDataV31Fetcher partyDataFetcher;
    private UKOpenBankingAis aisConfiguration;
    private UkOpenBankingApiClient apiClient;
    private IdentityDataMapper identityDataMapper;

    @Before
    public void setUp() {
        apiClient = mock(UkOpenBankingApiClient.class);
        aisConfiguration = mock(UKOpenBankingAis.class);
        identityDataMapper = mock(IdentityDataMapper.class);
        partyDataFetcher =
                new IdentityDataV31Fetcher(apiClient, aisConfiguration, identityDataMapper);
    }

    @Test
    public void doNotfetchPartyOnlyIfItsNotEnabledInConfig() {
        // when
        when(aisConfiguration.isPartyEndpointEnabled()).thenReturn(false);
        Optional<IdentityData> result = partyDataFetcher.fetchIdentityData();

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

        Optional<IdentityData> result = partyDataFetcher.fetchIdentityData();
        // then
        assertThat(result.get().getDateOfBirth()).isEqualTo(expectedResponse.getDateOfBirth());
        assertThat(result.get()).isEqualTo(expectedResponse);
    }

    @Test
    public void whenPartiesEndpointIsEnabled_fetchDataFromIt() {
        // given
        List<IdentityDataV31Entity> partiesResponse = PartyFixtures.parties();

        when(apiClient.fetchV31Parties("123ACC_ID")).thenReturn(partiesResponse);

        // when
        when(aisConfiguration.isAccountPartiesEndpointEnabled()).thenReturn(true);
        when(aisConfiguration.isAccountPartyEndpointEnabled()).thenReturn(true);
        List<IdentityDataV31Entity> partyListResult =
                partyDataFetcher.fetchAccountParties("123ACC_ID");

        // then
        assertThat(partyListResult).isEqualTo(partiesResponse);
    }

    @Test
    public void whenPartiesEndpointIsDisabled_fetchDataFromPartyEndpoint() {
        // given
        IdentityDataV31Entity partyResponse = PartyFixtures.party();
        when(apiClient.fetchV31Party("123ACC_ID")).thenReturn(Optional.of(partyResponse));

        // when
        when(aisConfiguration.isAccountPartiesEndpointEnabled()).thenReturn(false);
        when(aisConfiguration.isAccountPartyEndpointEnabled()).thenReturn(true);
        List<IdentityDataV31Entity> singlePartyResult =
                partyDataFetcher.fetchAccountParties("123ACC_ID");

        // then
        assertThat(singlePartyResult).isEqualTo(Collections.singletonList(partyResponse));
    }

    @Test
    public void whenAccountPartiesEndpointsAreDisabled_emptyListIsReturned() {
        // when
        when(aisConfiguration.isAccountPartyEndpointEnabled()).thenReturn(false);
        when(aisConfiguration.isAccountPartiesEndpointEnabled()).thenReturn(false);
        List<IdentityDataV31Entity> result = partyDataFetcher.fetchAccountParties("123ACC_ID");

        // then
        verifyZeroInteractions(apiClient);
        assertThat(result.isEmpty()).isTrue();
    }
}
