package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.PartyFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.TransactionalAccountFixtures;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(MockitoJUnitRunner.class)
public class PartyV31FetcherTest {

    @Mock private UkOpenBankingApiClient apiClient;
    @Mock private UkOpenBankingAisConfig aisConfig;
    @Mock private PersistentStorage persistentStorage;
    private PartyV31Fetcher fetcher;

    @Before
    public void setUp() {
        fetcher = new PartyV31Fetcher(apiClient, aisConfig, persistentStorage);
    }

    @Test
    public void fetchPartySuccessfully() {
        // given
        PartyV31Entity expectedParty = PartyFixtures.party();
        given(aisConfig.isPartyEndpointEnabled()).willReturn(true);
        given(persistentStorage.get(eq("last_SCA_time"), eq(String.class)))
                .willReturn(Optional.of(LocalDateTime.now().toString())); // sca not expired
        given(apiClient.fetchAccountParty()).willReturn(Optional.of(expectedParty));

        // when
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
        given(aisConfig.isAccountPartiesEndpointEnabled()).willReturn(true);
        given(persistentStorage.get(eq("last_SCA_time"), eq(String.class)))
                .willReturn(Optional.of(LocalDateTime.now().minusSeconds(10).toString()));
        given(apiClient.fetchAccountParties(eq(account.getAccountId())))
                .willReturn(partiesResponse);

        // when
        List<PartyV31Entity> partyListResult = fetcher.fetchAccountParties(account);

        // then
        assertThat(partyListResult.size()).isEqualTo(2);
        assertThat(partyListResult.get(0).getPartyId())
                .isEqualTo(partiesResponse.get(0).getPartyId());
        assertThat(partyListResult.get(1).getPartyId())
                .isEqualTo(partiesResponse.get(1).getPartyId());
    }

    @Test
    public void whenPartiesEndpointIsDisabled_fetchDataFromPartyEndpoint() {
        // given
        PartyV31Entity partyResponse = PartyFixtures.party();
        AccountEntity account = TransactionalAccountFixtures.currentAccount();
        given(apiClient.fetchAccountParty(eq(account.getAccountId())))
                .willReturn(Optional.of(partyResponse));
        given(aisConfig.isAccountPartiesEndpointEnabled()).willReturn(false);
        given(aisConfig.isAccountPartyEndpointEnabled()).willReturn(true);
        given(persistentStorage.get(eq("last_SCA_time"), eq(String.class)))
                .willReturn(Optional.of(LocalDateTime.now().toString())); // sca not expired

        // when
        List<PartyV31Entity> singlePartyResult = fetcher.fetchAccountParties(account);

        // then
        assertThat(singlePartyResult).isEqualTo(Collections.singletonList(partyResponse));
    }

    @Test
    public void whenAccountPartiesEndpointsAreDisabled_emptyListIsReturned() {
        // given
        given(persistentStorage.get(eq("last_SCA_time"), eq(String.class)))
                .willReturn(Optional.of(LocalDateTime.now().toString())); // sca not expired
        given(aisConfig.isAccountPartyEndpointEnabled()).willReturn(false);
        given(aisConfig.isAccountPartiesEndpointEnabled()).willReturn(false);

        // when
        List<PartyV31Entity> result = fetcher.fetchAccountParties(mock(AccountEntity.class));

        // then
        verifyNoInteractions(apiClient);
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void shouldReturnStoredPartiesWhenScaIsExpired() {
        // given
        AccountEntity account = TransactionalAccountFixtures.currentAccount();
        List<PartyV31Entity> data = new ArrayList<>();
        PartyV31Entity entity = new PartyV31Entity();
        entity.setPartyId("notRealId");
        entity.setName("anonymous");
        data.add(entity);
        given(persistentStorage.get(eq("last_SCA_time"), eq(String.class)))
                .willReturn(
                        Optional.of(
                                LocalDateTime.now()
                                        .minusDays(1)
                                        .toString())); // check if sca is expired
        given(persistentStorage.get(eq("recent_identity_data_list"), any(TypeReference.class)))
                .willReturn(Optional.of(data)); // restore parties

        // when
        List<PartyV31Entity> result = fetcher.fetchAccountParties(account);

        // then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getPartyId()).isEqualTo("notRealId");
        assertThat(result.get(0).getName()).isEqualTo("anonymous");
        verify(persistentStorage, times(1)).get("last_SCA_time", String.class);
        verify(persistentStorage, times(1))
                .get(eq("recent_identity_data_list"), any(TypeReference.class));
    }

    @Test
    public void shouldNotFetchNeitherRestoreIfScaExpiredAndNothingStored() {
        // given
        AccountEntity account = TransactionalAccountFixtures.currentAccount();
        given(persistentStorage.get(eq("last_SCA_time"), eq(String.class)))
                .willReturn(
                        Optional.of(
                                LocalDateTime.now().minusDays(1).toString())); //  sca is expired
        given(
                        persistentStorage.get(
                                eq("recent_identity_data_list"), Mockito.any(TypeReference.class)))
                .willReturn(Optional.of(Collections.emptyList())); // restoreParties

        // when
        List<PartyV31Entity> result = fetcher.fetchAccountParties(account);

        // then
        assertThat(result.size()).isZero();
    }
}
