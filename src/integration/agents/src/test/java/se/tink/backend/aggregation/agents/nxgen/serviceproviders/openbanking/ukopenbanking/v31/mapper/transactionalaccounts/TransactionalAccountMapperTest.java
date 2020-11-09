package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.IdentifierFixtures.ibanIdentifier;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.IdentifierFixtures.sortCodeIdentifier;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.TransactionalAccountFixtures.currentAccount;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.TransactionalAccountFixtures.savingsAccount;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.IdentifierFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.PartyFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.TransactionalAccountFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.identifier.DefaultIdentifierMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionalAccountMapperTest {

    private TransactionalAccountMapper mapper;
    private TransactionalAccountBalanceMapper balanceMapper;
    private DefaultIdentifierMapper identifierMapper;

    @Before
    public void setUp() {
        balanceMapper = Mockito.mock(TransactionalAccountBalanceMapper.class);
        identifierMapper = Mockito.mock(DefaultIdentifierMapper.class);
        mapper = new TransactionalAccountMapper(balanceMapper, identifierMapper);

        when(balanceMapper.getAccountBalance(anyCollection()))
                .thenReturn(ExactCurrencyAmount.of(12345, "EUR"));
        when(identifierMapper.mapIdentifier(any())).thenCallRealMethod();
        when(identifierMapper.getTransactionalAccountPrimaryIdentifier(anyList(), anyList()))
                .thenReturn(sortCodeIdentifier());
    }

    @Test
    public void shouldMapBalances_usingBalanceMapper() {
        // given
        ExactCurrencyAmount accBalance = ExactCurrencyAmount.of(-123.22, "GBP");
        ExactCurrencyAmount availableBalance = ExactCurrencyAmount.of(432.44, "EUR");
        ExactCurrencyAmount availableCredit = ExactCurrencyAmount.of(888, "GBP");
        ExactCurrencyAmount creditLimit = ExactCurrencyAmount.of(11.888, "PLN");

        // when
        when(balanceMapper.getAccountBalance(anyCollection())).thenReturn(accBalance);
        when(balanceMapper.getAvailableBalance(anyCollection()))
                .thenReturn(Optional.of(availableBalance), Optional.empty());
        when(balanceMapper.calculateAvailableCredit(anyCollection()))
                .thenReturn(Optional.of(availableCredit), Optional.empty());
        when(balanceMapper.calculateCreditLimit(anyCollection()))
                .thenReturn(Optional.of(creditLimit), Optional.empty());

        TransactionalAccount result1 =
                mapper.map(currentAccount(), Collections.emptyList(), Collections.emptyList())
                        .get();

        TransactionalAccount result2 =
                mapper.map(currentAccount(), Collections.emptyList(), Collections.emptyList())
                        .get();

        // then
        assertBalance(result1, accBalance, availableBalance, availableCredit, creditLimit);
        assertBalance(result2, accBalance, null, null, null);
    }

    private void assertBalance(
            TransactionalAccount resultBalance,
            ExactCurrencyAmount accBalance,
            ExactCurrencyAmount availableBalance,
            ExactCurrencyAmount availableCredit,
            ExactCurrencyAmount creditLimit) {
        assertThat(resultBalance.getExactBalance()).isEqualTo(accBalance);
        assertThat(resultBalance.getExactAvailableBalance()).isEqualTo(availableBalance);
        assertThat(resultBalance.getExactAvailableCredit()).isEqualTo(availableCredit);
        assertThat(resultBalance.getExactCreditLimit()).isEqualTo(creditLimit);
    }

    @Test
    public void shouldUseAccountNumberAndOwnerName_fromPrimaryIdentifier() {
        // given
        AccountIdentifierEntity expectedIdentifier = ibanIdentifier();

        // when
        when(identifierMapper.getTransactionalAccountPrimaryIdentifier(anyList(), anyList()))
                .thenReturn(expectedIdentifier);
        TransactionalAccount mappingResult =
                mapper.map(currentAccount(), Collections.emptyList(), Collections.emptyList())
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
        AccountEntity currentAccount = currentAccount();
        AccountEntity savingsAccount = savingsAccount();

        // when
        TransactionalAccount currentAccountResult =
                mapper.map(currentAccount, Collections.emptyList(), Collections.emptyList()).get();
        TransactionalAccount savingsAccountResult =
                mapper.map(savingsAccount, Collections.emptyList(), Collections.emptyList()).get();

        // then
        assertThat(currentAccountResult.getType()).isEqualByComparingTo(AccountTypes.CHECKING);
        assertThat(savingsAccountResult.getType()).isEqualByComparingTo(AccountTypes.SAVINGS);
    }

    @Test
    public void shouldCorrectlyMapApiIdentifier() {
        // given
        AccountEntity input = currentAccount();

        // when
        TransactionalAccount result =
                mapper.map(input, Collections.emptyList(), Collections.emptyList()).get();

        // then
        assertThat(result.getApiIdentifier()).isEqualTo(input.getAccountId());
    }

    @Test
    public void shouldMapAllIds() {
        // given
        AccountEntity account = currentAccount();

        // when
        TransactionalAccount result =
                mapper.map(account, Collections.emptyList(), Collections.emptyList()).get();

        // then
        List<AccountIdentifier> expectedMappedIdentifiers =
                account.getIdentifiers().stream()
                        .map(identifierMapper::mapIdentifier)
                        .collect(Collectors.toList());
        assertThat(result.getIdentifiers()).containsAll(expectedMappedIdentifiers);
    }

    @Test
    public void holderName_shouldBeOneOfPartyOrFromIdentifierOwnerName() {
        // given
        AccountEntity account = TransactionalAccountFixtures.currentAccount();
        AccountIdentifierEntity primaryId = IdentifierFixtures.ibanIdentifier();
        List<IdentityDataV31Entity> parties = PartyFixtures.parties();

        // when
        when(identifierMapper.getCreditCardIdentifier(anyCollection())).thenReturn(primaryId);
        TransactionalAccount mappingResult =
                mapper.map(account, Collections.emptyList(), parties).get();

        // then
        List<String> allPosibleHolders =
                parties.stream().map(IdentityDataV31Entity::getName).collect(Collectors.toList());
        allPosibleHolders.add(primaryId.getOwnerName());

        assertThat(mappingResult.getHolderName().toString()).isIn(allPosibleHolders);
    }
}
