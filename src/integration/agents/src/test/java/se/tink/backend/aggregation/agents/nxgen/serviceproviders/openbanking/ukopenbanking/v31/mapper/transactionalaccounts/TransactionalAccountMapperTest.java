package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.IdentifierFixtures.ibanIdentifier;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.IdentifierFixtures.sortCodeIdentifier;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.TransactionalAccountFixtures.currentAccount;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.TransactionalAccountFixtures.savingsAccount;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.TransactionalAccountFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.identifier.IdentifierMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionalAccountMapperTest {

    private TransactionalAccountMapper mapper;
    private TransactionalAccountBalanceMapper balanceMapper;
    private IdentifierMapper identifierMapper;

    @Before
    public void setUp() {
        balanceMapper = Mockito.mock(TransactionalAccountBalanceMapper.class);
        identifierMapper = Mockito.mock(IdentifierMapper.class);
        mapper = new TransactionalAccountMapper(balanceMapper, identifierMapper);

        when(balanceMapper.getAccountBalance(anyCollection()))
                .thenReturn(ExactCurrencyAmount.of(12345d, "EUR"));
        when(identifierMapper.mapIdentifier(any())).thenCallRealMethod();
        when(identifierMapper.getTransactionalAccountPrimaryIdentifier(anyList()))
                .thenReturn(sortCodeIdentifier());
    }

    @Test
    public void shouldMapBalances_usingBalanceMapper() {
        // given
        List<AccountBalanceEntity> balances = mock(List.class);
        ExactCurrencyAmount expectedBalance = ExactCurrencyAmount.of(123.22d, "GBP");

        // when
        when(balanceMapper.getAccountBalance(balances)).thenReturn(expectedBalance);

        TransactionalAccount mappingResult =
                mapper.map(TransactionalAccountFixtures.currentAccount(), balances, anyString())
                        .get();

        // then
        assertThat(mappingResult.getExactBalance()).isEqualByComparingTo(expectedBalance);
    }

    @Test
    public void shouldUseAccountNumberAndOwnerName_fromPrimaryIdentifier() {
        // given
        AccountIdentifierEntity expectedIdentifier = ibanIdentifier();

        // when
        when(identifierMapper.getTransactionalAccountPrimaryIdentifier(anyList()))
                .thenReturn(expectedIdentifier);
        TransactionalAccount mappingResult =
                mapper.map(TransactionalAccountFixtures.currentAccount(), anyCollection(), "")
                        .get();

        // then
        assertThat(mappingResult.getAccountNumber())
                .isEqualTo(expectedIdentifier.getIdentification());
        assertThat(mappingResult.getIdModule().getAccountNumber())
                .isEqualTo(expectedIdentifier.getIdentification());
        assertThat(mappingResult.getHolderName().toString())
                .isEqualTo(expectedIdentifier.getOwnerName());
    }

    @Test
    public void shouldCorrectlyPickAccountType() {
        // given
        AccountEntity currentAccount = TransactionalAccountFixtures.currentAccount();
        AccountEntity savingsAccount = savingsAccount();

        // when
        TransactionalAccount currentAccountResult =
                mapper.map(currentAccount, anyCollection(), "").get();
        TransactionalAccount savingsAccountResult =
                mapper.map(savingsAccount, anyCollection(), "").get();

        // then
        assertThat(currentAccountResult.getType()).isEqualByComparingTo(AccountTypes.CHECKING);
        assertThat(savingsAccountResult.getType()).isEqualByComparingTo(AccountTypes.SAVINGS);
    }

    @Test
    public void shouldCorrectlyMapRemainingApiIdentifier() {
        // given
        AccountEntity input = TransactionalAccountFixtures.currentAccount();

        // when
        TransactionalAccount result = mapper.map(input, anyCollection(), "").get();

        // then
        assertThat(result.getApiIdentifier()).isEqualTo(input.getAccountId());
    }

    @Test
    public void shouldMapAllIds() {
        // given
        AccountEntity account = currentAccount();

        // when
        TransactionalAccount result = mapper.map(account, anyCollection(), "").get();

        // then
        List<AccountIdentifier> expectedMappedIdentifiers =
                account.getIdentifiers().stream()
                        .map(identifierMapper::mapIdentifier)
                        .collect(Collectors.toList());
        assertThat(result.getIdentifiers()).containsAll(expectedMappedIdentifiers);
    }
}
