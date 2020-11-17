package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.PartyDataV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountTypeMapper;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.fetcher.BarclaysPartyDataFetcher;

public class BarclaysPartyDataFetcherTest {

    private BarclaysPartyDataFetcher barclaysPartyDataFetcher;
    private AccountTypeMapper accountTypeMapper;
    private PartyDataV31Fetcher basePartyFetcher;

    @Before
    public void setUp() {
        accountTypeMapper = mock(AccountTypeMapper.class);
        basePartyFetcher = mock(PartyDataV31Fetcher.class);
        barclaysPartyDataFetcher =
                new BarclaysPartyDataFetcher(accountTypeMapper, basePartyFetcher);
    }

    @Test
    public void shouldNotFetchDataForCreditCards() {
        // when
        when(accountTypeMapper.getAccountType(any())).thenReturn(AccountTypes.CREDIT_CARD);
        when(accountTypeMapper.getAccountOwnershipType(any()))
                .thenReturn(AccountOwnershipType.PERSONAL);
        List<IdentityDataV31Entity> result =
                barclaysPartyDataFetcher.fetchAccountParties(mock(AccountEntity.class));

        // then
        assertThat(result).isEmpty();
        verifyZeroInteractions(basePartyFetcher);
    }

    @Test
    public void shouldNotFetchDataForBusinessAccounts() {
        // when
        when(accountTypeMapper.getAccountType(any())).thenReturn(AccountTypes.CHECKING);
        when(accountTypeMapper.getAccountOwnershipType(any()))
                .thenReturn(AccountOwnershipType.BUSINESS);
        List<IdentityDataV31Entity> result =
                barclaysPartyDataFetcher.fetchAccountParties(mock(AccountEntity.class));

        // then
        assertThat(result).isEmpty();
        verifyZeroInteractions(basePartyFetcher);
    }
}
