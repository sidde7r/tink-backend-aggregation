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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.PartyFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.TransactionalAccountFixtures;

public class PartyV31FetcherTest {

    private PartyV31Fetcher fetcher;
    private UkOpenBankingApiClient apiClient;
    private UkOpenBankingAisConfig aisConfig;

    @Before
    public void setUp() {
        apiClient = mock(UkOpenBankingApiClient.class);
        aisConfig = mock(UkOpenBankingAisConfig.class);
        fetcher = new PartyV31Fetcher(apiClient, aisConfig);
    }

    @Test
    public void fetchPartySuccessfully() {
        // given
        PartyV31Entity expectedParty = PartyFixtures.party();

        // when
        when(aisConfig.isPartyEndpointEnabled()).thenReturn(true);
        when(apiClient.fetchV31Party()).thenReturn(Optional.of(expectedParty));

        Optional<PartyV31Entity> result = fetcher.fetchParty();

        // then
        assertThat(result.get().getName()).isEqualTo(expectedParty.getName());
        assertThat(result.get()).isEqualTo(expectedParty);
    }

    @Test
    public void whenPartiesEndpointIsEnabled_fetchDataFromIt() {
        // given
        AccountEntity account = TransactionalAccountFixtures.currentAccount();
        List<PartyV31Entity> partiesResponse = PartyFixtures.parties();

        // when
        when(apiClient.fetchV31Parties(eq(account.getAccountId()))).thenReturn(partiesResponse);
        when(aisConfig.isAccountPartiesEndpointEnabled()).thenReturn(true);
        when(aisConfig.isAccountPartyEndpointEnabled()).thenReturn(true);
        List<PartyV31Entity> partyListResult = fetcher.fetchAccountParties(account);

        // then
        assertThat(partyListResult).isEqualTo(partiesResponse);
    }

    @Test
    public void whenPartiesEndpointIsDisabled_fetchDataFromPartyEndpoint() {
        // given
        PartyV31Entity partyResponse = PartyFixtures.party();
        AccountEntity account = TransactionalAccountFixtures.currentAccount();

        // when
        when(apiClient.fetchV31Party(eq(account.getAccountId())))
                .thenReturn(Optional.of(partyResponse));
        when(aisConfig.isAccountPartiesEndpointEnabled()).thenReturn(false);
        when(aisConfig.isAccountPartyEndpointEnabled()).thenReturn(true);
        List<PartyV31Entity> singlePartyResult = fetcher.fetchAccountParties(account);

        // then
        assertThat(singlePartyResult).isEqualTo(Collections.singletonList(partyResponse));
    }

    @Test
    public void whenAccountPartiesEndpointsAreDisabled_emptyListIsReturned() {
        // when
        when(aisConfig.isAccountPartyEndpointEnabled()).thenReturn(false);
        when(aisConfig.isAccountPartiesEndpointEnabled()).thenReturn(false);
        List<PartyV31Entity> result = fetcher.fetchAccountParties(mock(AccountEntity.class));

        // then
        verifyZeroInteractions(apiClient);
        assertThat(result.isEmpty()).isTrue();
    }
}
