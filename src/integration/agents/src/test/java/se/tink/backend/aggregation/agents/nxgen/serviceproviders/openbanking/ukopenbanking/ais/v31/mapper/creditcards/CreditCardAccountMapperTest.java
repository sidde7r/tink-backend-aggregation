package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.BalanceFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.CreditCardFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.IdentifierFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.PartyFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.DefaultIdentifierMapper;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.account.identifiers.PaymentCardNumberIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CreditCardAccountMapperTest {

    private CreditCardAccountMapper mapper;
    private CreditCardBalanceMapper balanceMapper;
    private DefaultIdentifierMapper identifierMapper;

    @Before
    public void setUp() {
        balanceMapper = Mockito.mock(CreditCardBalanceMapper.class);
        when(balanceMapper.getAccountBalance(anyCollection()))
                .thenReturn(ExactCurrencyAmount.of(123, "GBP"));
        when(balanceMapper.getAvailableCredit(anyCollection()))
                .thenReturn(ExactCurrencyAmount.of(1, "GBP"));

        identifierMapper = mock(DefaultIdentifierMapper.class);
        when(identifierMapper.mapIdentifier(any())).thenCallRealMethod();
        when(identifierMapper.getCreditCardIdentifier(anyCollection()))
                .thenReturn(IdentifierFixtures.panIdentifier());
        mapper = new CreditCardAccountMapper(balanceMapper, identifierMapper);
    }

    @Test
    public void shouldMapBalances_usingBalanceMapper() {
        // given
        List<AccountBalanceEntity> balances =
                ImmutableList.of(
                        BalanceFixtures.closingBookedBalance(),
                        BalanceFixtures.interimAvailableBalance());

        // when
        when(identifierMapper.getCreditCardIdentifier(anyCollection()))
                .thenReturn(IdentifierFixtures.panIdentifier());
        ExactCurrencyAmount expectedAccountBalance = ExactCurrencyAmount.of(-333.11, "GBP");
        when(balanceMapper.getAccountBalance(balances)).thenReturn(expectedAccountBalance);

        ExactCurrencyAmount expectedAvailableCredit = ExactCurrencyAmount.of(123.123, "EUR");
        when(balanceMapper.getAvailableCredit(balances)).thenReturn(expectedAvailableCredit);

        Optional<CreditCardAccount> mappingResult =
                mapper.map(
                        CreditCardFixtures.creditCardAccount(), balances, Collections.emptyList());

        // then
        assertThat(mappingResult.get().getExactBalance())
                .isEqualByComparingTo(expectedAccountBalance);
        assertThat(mappingResult.get().getExactAvailableCredit())
                .isEqualTo(expectedAvailableCredit);
    }

    @Test
    public void shouldUseIds_fromIdentifierMapper() {
        // given
        AccountIdentifierEntity expectedIdentifier = IdentifierFixtures.panIdentifier();
        // when
        when(identifierMapper.getCreditCardIdentifier(anyCollection()))
                .thenReturn(expectedIdentifier);

        CreditCardAccount mappingResult =
                mapper.map(
                                CreditCardFixtures.creditCardAccount(),
                                Collections.emptyList(),
                                Collections.emptyList())
                        .get();

        // then
        assertThat(mappingResult.getIdentifiers())
                .containsOnly(
                        new PaymentCardNumberIdentifier(expectedIdentifier.getIdentification()));
        assertThat(mappingResult.getIdModule().getUniqueId())
                .isEqualTo(StringUtils.right(expectedIdentifier.getIdentification(), 4));
        assertThat(mappingResult.getIdModule().getAccountNumber())
                .isEqualTo(expectedIdentifier.getIdentification());
        assertThat(mappingResult.getCardModule().getCardNumber())
                .isEqualTo(expectedIdentifier.getIdentification());
    }

    @Test
    public void shouldUseHolderName_fromAccountIdentifier() {
        // given
        AccountIdentifierEntity expectedIdentifier = IdentifierFixtures.panIdentifier();
        // when
        when(identifierMapper.getCreditCardIdentifier(anyCollection()))
                .thenReturn(expectedIdentifier);
        CreditCardAccount mappingResult =
                mapper.map(
                                CreditCardFixtures.creditCardAccount(),
                                Collections.emptyList(),
                                Collections.emptyList())
                        .get();

        assertThat(mappingResult.getHolderName().toString())
                .isEqualTo(expectedIdentifier.getOwnerName());
    }

    @Test
    public void holderName_shouldBeOneOfPartyOrFromIdentifierOwnerName() {
        // given
        AccountEntity creditCardAccount = CreditCardFixtures.creditCardAccount();
        AccountIdentifierEntity primaryId = IdentifierFixtures.panIdentifier();
        List<PartyV31Entity> parties = PartyFixtures.parties();

        // when
        when(identifierMapper.getCreditCardIdentifier(anyCollection())).thenReturn(primaryId);
        CreditCardAccount mappingResult =
                mapper.map(creditCardAccount, Collections.emptyList(), parties).get();

        // then
        List<String> allPossibleHolders =
                parties.stream().map(PartyV31Entity::getName).collect(Collectors.toList());
        allPossibleHolders.add(primaryId.getOwnerName());

        assertThat(mappingResult.getHolderName().toString()).isIn(allPossibleHolders);
    }
}
