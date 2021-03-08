package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.PartyDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.ScaExpirationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.PartyFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.PartyV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountTypeMapper;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.fetcher.BarclaysPartyFetcher;

public class BarclaysPartyDataFetcherTest {

    private BarclaysPartyFetcher barclaysFetcher;
    private AccountTypeMapper accountTypeMapper;
    private PartyFetcher baseFetcher;
    private ScaExpirationValidator scaValidator;
    private PartyDataStorage storage;
    private UkOpenBankingAisConfig config;

    @Before
    public void setUp() {
        config = mock(UkOpenBankingAisConfig.class);
        accountTypeMapper = mock(AccountTypeMapper.class);
        baseFetcher = mock(PartyV31Fetcher.class);
        scaValidator = mock(ScaExpirationValidator.class);
        storage = mock(PartyDataStorage.class);

        barclaysFetcher =
                new BarclaysPartyFetcher(
                        mock(UkOpenBankingApiClient.class),
                        config,
                        storage,
                        accountTypeMapper,
                        scaValidator);
    }

    @Test
    public void shouldNotFetchDataForCreditCards() {
        // when
        when(accountTypeMapper.getAccountType(any())).thenReturn(AccountTypes.CREDIT_CARD);
        when(accountTypeMapper.getAccountOwnershipType(any()))
                .thenReturn(AccountOwnershipType.PERSONAL);
        List<PartyV31Entity> result =
                barclaysFetcher.fetchAccountParties(mock(AccountEntity.class));

        // then
        assertThat(result).isEmpty();
        verifyZeroInteractions(baseFetcher);
    }

    @Test
    public void shouldNotFetchDataForBusinessAccounts() {
        // when
        when(accountTypeMapper.getAccountType(any())).thenReturn(AccountTypes.CHECKING);
        when(accountTypeMapper.getAccountOwnershipType(any()))
                .thenReturn(AccountOwnershipType.BUSINESS);
        List<PartyV31Entity> result =
                barclaysFetcher.fetchAccountParties(mock(AccountEntity.class));

        // then
        assertThat(result).isEmpty();
        verifyZeroInteractions(baseFetcher);
    }

    @Test
    public void restoreIdentitiesFromPersistentStorageIfScaExpired() {
        // given
        List<PartyV31Entity> partiesResponse = PartyFixtures.parties();
        AccountEntity account = TransactionalAccountFixtures.currentAccount();

        // when
        when(accountTypeMapper.getAccountType(any())).thenReturn(AccountTypes.CHECKING);
        when(accountTypeMapper.getAccountOwnershipType(any()))
                .thenReturn(AccountOwnershipType.PERSONAL);
        when(config.isAccountPartiesEndpointEnabled()).thenReturn(true);
        when(scaValidator.isScaExpired()).thenReturn(true);
        when(storage.restoreParties()).thenReturn(partiesResponse);

        List<PartyV31Entity> result = barclaysFetcher.fetchAccountParties(account);

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0)).isEqualTo(partiesResponse.get(0));
        assertThat(result.get(1)).isEqualTo(partiesResponse.get(1));
    }

    @Test
    public void shouldNotFetchNeitherRestoreIfScaExpiredAndNothingStored() {
        // given
        AccountEntity account = TransactionalAccountFixtures.currentAccount();

        // when
        when(config.isAccountPartiesEndpointEnabled()).thenReturn(true);
        when(accountTypeMapper.getAccountType(any())).thenReturn(AccountTypes.CHECKING);
        when(accountTypeMapper.getAccountOwnershipType(any()))
                .thenReturn(AccountOwnershipType.PERSONAL);
        when(scaValidator.isScaExpired()).thenReturn(true);
        when(storage.restoreParties()).thenReturn(Collections.emptyList());

        List<PartyV31Entity> result = barclaysFetcher.fetchAccountParties(account);

        // then
        assertThat(result.size()).isZero();
    }
}
