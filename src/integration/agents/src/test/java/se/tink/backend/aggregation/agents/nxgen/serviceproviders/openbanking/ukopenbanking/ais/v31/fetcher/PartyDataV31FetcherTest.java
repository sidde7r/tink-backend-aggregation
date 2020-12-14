package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.PartyFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.TransactionalAccountFixtures;

public class PartyDataV31FetcherTest {

    private PartyDataFetcher partyDataFetcher;
    private UkOpenBankingAisConfiguration aisConfiguration;
    private UkOpenBankingApiClient apiClient;

    @Before
    public void setUp() {
        apiClient = mock(UkOpenBankingApiClient.class);
        aisConfiguration = mock(UkOpenBankingAisConfiguration.class);
        partyDataFetcher = new PartyDataV31Fetcher(apiClient, aisConfiguration);
    }

    @Test
    public void whenPartiesEndpointIsEnabled_fetchDataFromIt() {
        // given
        AccountEntity account = TransactionalAccountFixtures.currentAccount();
        List<IdentityDataV31Entity> partiesResponse = PartyFixtures.parties();

        // when
        when(apiClient.fetchV31Parties(eq(account.getAccountId()))).thenReturn(partiesResponse);
        when(aisConfiguration.isAccountPartiesEndpointEnabled()).thenReturn(true);
        when(aisConfiguration.isAccountPartyEndpointEnabled()).thenReturn(true);
        List<IdentityDataV31Entity> partyListResult = partyDataFetcher.fetchAccountParties(account);

        // then
        assertThat(partyListResult).isEqualTo(partiesResponse);
    }

    @Test
    public void whenPartiesEndpointIsDisabled_fetchDataFromPartyEndpoint() {
        // given
        IdentityDataV31Entity partyResponse = PartyFixtures.party();
        AccountEntity account = TransactionalAccountFixtures.currentAccount();

        // when
        when(apiClient.fetchV31Party(eq(account.getAccountId())))
                .thenReturn(Optional.of(partyResponse));
        when(aisConfiguration.isAccountPartiesEndpointEnabled()).thenReturn(false);
        when(aisConfiguration.isAccountPartyEndpointEnabled()).thenReturn(true);
        List<IdentityDataV31Entity> singlePartyResult =
                partyDataFetcher.fetchAccountParties(account);

        // then
        assertThat(singlePartyResult).isEqualTo(Collections.singletonList(partyResponse));
    }

    @Test
    public void whenAccountPartiesEndpointsAreDisabled_emptyListIsReturned() {
        // when
        when(aisConfiguration.isAccountPartyEndpointEnabled()).thenReturn(false);
        when(aisConfiguration.isAccountPartiesEndpointEnabled()).thenReturn(false);
        List<IdentityDataV31Entity> result =
                partyDataFetcher.fetchAccountParties(mock(AccountEntity.class));

        // then
        verifyZeroInteractions(apiClient);
        assertThat(result.isEmpty()).isTrue();
    }
}
