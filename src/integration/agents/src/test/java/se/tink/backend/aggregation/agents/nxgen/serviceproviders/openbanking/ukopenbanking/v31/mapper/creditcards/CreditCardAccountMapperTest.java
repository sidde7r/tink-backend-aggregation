package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.creditcards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.BalanceFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.CreditCardFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fixtures.IdentifierFixtures;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.identifier.IdentifierMapper;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.account.identifiers.PaymentCardNumberIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CreditCardAccountMapperTest {

    private CreditCardAccountMapper mapper;
    private CreditCardBalanceMapper balanceMapper;
    private IdentifierMapper identifierMapper;

    @Before
    public void setUp() {
        balanceMapper = Mockito.mock(CreditCardBalanceMapper.class);
        when(balanceMapper.getAccountBalance(anyCollection()))
                .thenReturn(ExactCurrencyAmount.of(123d, "GBP"));
        when(balanceMapper.getAvailableCredit(anyCollection()))
                .thenReturn(ExactCurrencyAmount.of(1d, "GBP"));

        identifierMapper = mock(IdentifierMapper.class);
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
    public void shouldUseIds_fromIdentifierMapper() {
        // given
        AccountIdentifierEntity expectedIdentifier = IdentifierFixtures.panIdentifier();
        // when
        when(identifierMapper.getCreditCardIdentifier(anyCollection()))
                .thenReturn(expectedIdentifier);

        CreditCardAccount mappingResult =
                mapper.map(
                        CreditCardFixtures.creditCardAccount(),
                        mock(Collection.class),
                        "somePartyName");

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
                        mock(Collection.class),
                        "somePartyName");

        assertThat(mappingResult.getHolderName().toString())
                .isEqualTo(expectedIdentifier.getOwnerName());
    }

    @Test
    public void shouldUsePartyNameAsHolderName_whenOwnerNameIsNotPresentInIdentifier() {
        // given
        AccountEntity creditCardAccount = CreditCardFixtures.creditCardAccount();

        AccountIdentifierEntity identifierWithoutOwnerName = IdentifierFixtures.panIdentifier();
        identifierWithoutOwnerName.setOwnerName(null);

        // when
        when(identifierMapper.getCreditCardIdentifier(anyCollection()))
                .thenReturn(identifierWithoutOwnerName);
        creditCardAccount.setIdentifiers(Collections.singletonList(identifierWithoutOwnerName));
        CreditCardAccount mappingResult =
                mapper.map(creditCardAccount, ImmutableList.of(), "somePartyName");

        assertThat(mappingResult.getHolderName().toString()).isEqualTo("somePartyName");
    }
}
