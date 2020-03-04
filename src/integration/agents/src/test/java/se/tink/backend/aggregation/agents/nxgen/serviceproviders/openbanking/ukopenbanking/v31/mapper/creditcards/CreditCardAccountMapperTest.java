package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.creditcards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.BalanceFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.CreditCardFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.IdentifierFixtures;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.account.identifiers.PaymentCardNumberIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CreditCardAccountMapperTest {

    private CreditCardAccountMapper mapper;
    private DefaultCreditCardBalanceMapper balanceMapper;

    @Before
    public void setUp() {
        balanceMapper = Mockito.mock(DefaultCreditCardBalanceMapper.class);
        when(balanceMapper.getAccountBalance(anyCollection()))
                .thenReturn(ExactCurrencyAmount.of(123d, "GBP"));
        when(balanceMapper.getAvailableCredit(anyCollection()))
                .thenReturn(ExactCurrencyAmount.of(1d, "GBP"));

        mapper = new CreditCardAccountMapper(balanceMapper);
    }

    @Test
    public void shouldMapBalances_usingBalanceMapper() {
        // given
        List<AccountBalanceEntity> balances =
                ImmutableList.of(
                        BalanceFixtures.closingBookedBalance(),
                        BalanceFixtures.interimAvailableBalance());

        // when
        ExactCurrencyAmount expectedAccountBalance = ExactCurrencyAmount.of(-333.11d, "GBP");
        when(balanceMapper.getAccountBalance(balances)).thenReturn(expectedAccountBalance);

        ExactCurrencyAmount expectedAvailableCredit = ExactCurrencyAmount.of(123.123d, "EUR");
        when(balanceMapper.getAvailableCredit(balances)).thenReturn(expectedAvailableCredit);

        CreditCardAccount mappingResult =
                mapper.map(CreditCardFixtures.creditCardAccount(), balances, anyString());

        // then
        assertThat(mappingResult.getExactBalance()).isEqualByComparingTo(expectedAccountBalance);
        assertThat(mappingResult.getExactAvailableCredit()).isEqualTo(expectedAvailableCredit);
    }

    @Test
    public void shouldUseIds_fromPANIdentifier() {
        // when
        CreditCardAccount mappingResult =
                mapper.map(CreditCardFixtures.creditCardAccount(), ImmutableList.of(), "partyName");

        // then
        assertThat(mappingResult.getIdentifiers())
                .contains(new PaymentCardNumberIdentifier("************1234"));
        assertThat(mappingResult.getIdModule().getUniqueId()).isEqualTo("1234");
        assertThat(mappingResult.getIdModule().getAccountNumber()).isEqualTo("************1234");
        assertThat(mappingResult.getCardModule().getCardNumber()).isEqualTo("************1234");
    }

    @Test
    public void shouldUseHolderName_fromPANIdentifier() {
        CreditCardAccount mappingResult =
                mapper.map(CreditCardFixtures.creditCardAccount(), ImmutableList.of(), anyString());

        assertThat(mappingResult.getHolderName().toString()).isEqualTo("MR MYSZO-JELEN");
    }

    @Test
    public void shouldUsePartyNameAsHolderName_whenOwnerNameIsNotPresentInIdentifier() {
        // given
        AccountEntity creditCardAccount = CreditCardFixtures.creditCardAccount();

        AccountIdentifierEntity identifierWithoutOwnerName = IdentifierFixtures.panIdentifier();
        identifierWithoutOwnerName.setOwnerName(null);

        // when
        creditCardAccount.setIdentifiers(Collections.singletonList(identifierWithoutOwnerName));
        CreditCardAccount mappingResult =
                mapper.map(creditCardAccount, ImmutableList.of(), "somePartyName");

        assertThat(mappingResult.getHolderName().toString()).isEqualTo("somePartyName");
    }
}
