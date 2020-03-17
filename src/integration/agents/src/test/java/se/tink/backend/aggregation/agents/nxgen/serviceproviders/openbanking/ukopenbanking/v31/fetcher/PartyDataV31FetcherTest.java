package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UKOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.PartyFixtures;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class PartyDataV31FetcherTest {

    private PartyDataV31Fetcher partyDataFetcher;
    private UKOpenBankingAis aisConfiguration;
    private UkOpenBankingApiClient apiClient;

    @Before
    public void setUp() {
        apiClient = mock(UkOpenBankingApiClient.class);
        aisConfiguration = mock(UKOpenBankingAis.class);
        partyDataFetcher = new PartyDataV31Fetcher(apiClient, aisConfiguration);
    }

    @Test
    public void DoNotfetchPartyOnlyIfItsNotEnabledInConfig() {
        // when
        when(aisConfiguration.isPartyEndpointEnabled()).thenReturn(false);
        Optional<IdentityDataV31Entity> result = partyDataFetcher.fetchParty();

        // then
        verifyZeroInteractions(apiClient);
        assertThat(result.isPresent()).isFalse();
    }

    @Test
    public void fetchPartReturnsCorrectResult() {
        // given
        URL dummyBaseURL = new URL("https://dummy.com");
        IdentityDataV31Entity expectedResponse = PartyFixtures.party();

        // when
        when(aisConfiguration.isPartyEndpointEnabled()).thenReturn(true);
        when(aisConfiguration.getApiBaseURL()).thenReturn(dummyBaseURL);
        when(apiClient.fetchV31Party()).thenReturn(Optional.of(expectedResponse));

        Optional<IdentityDataV31Entity> result = partyDataFetcher.fetchParty();
        // then
        assertThat(result.get()).isEqualTo(expectedResponse);
    }

    @Test
    public void fetchDataFromPartiesEndpoint_otherwiseFromPartyEndpoint() {
        // given
        URL dummyBaseURL = new URL("https://dummy.com");
        IdentityDataV31Entity partyResponse = PartyFixtures.party();
        List<IdentityDataV31Entity> partiesResponse = PartyFixtures.parties();

        when(aisConfiguration.getApiBaseURL()).thenReturn(dummyBaseURL);
        when(apiClient.fetchV31Parties("123ACC_ID")).thenReturn(partiesResponse);
        when(apiClient.fetchV31Party("123ACC_ID")).thenReturn(Optional.of(partyResponse));

        // when
        when(aisConfiguration.isAccountPartiesEndpointEnabled()).thenReturn(true);
        when(aisConfiguration.isAccountPartyEndpointEnabled()).thenReturn(true);
        List<IdentityDataV31Entity> partyListResult =
                partyDataFetcher.fetchAccountParties("123ACC_ID");

        // then
        assertThat(partyListResult).isEqualTo(partiesResponse);

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
