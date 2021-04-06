package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleTestFixtures.AMOUNT_1;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleTestFixtures.AMOUNT_2;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleTestFixtures.CURRENCY;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleTestFixtures.IBAN;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleTestFixtures.NAME;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleTestFixtures.RESOURCE_ID;

import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountIdentificationDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceAmountBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.CashAccountType;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class LaBanquePostaleAccountConverterTest {

    private LaBanquePostaleAccountConverter laBanquePostaleAccountConverter;

    @Before
    public void setUp() {

        laBanquePostaleAccountConverter = new LaBanquePostaleAccountConverter();
    }

    @Test
    public void shouldConvertAccountResponseForTwoBalances() {
        // given
        final AccountEntity accountEntity = createAccountEntityMock();
        final BalanceBaseEntity clbdBalance =
                createBalanceBaseEntity(BerlinGroupConstants.Accounts.CLBD, AMOUNT_1);
        final BalanceBaseEntity xpcdBalance =
                createBalanceBaseEntity(BerlinGroupConstants.Accounts.XPCD, AMOUNT_2);
        final List<BalanceBaseEntity> balances = Arrays.asList(clbdBalance, xpcdBalance);

        when(accountEntity.getBalances()).thenReturn(balances);

        final AccountResponse accountResponse = mock(AccountResponse.class);

        when(accountResponse.getAccounts()).thenReturn(Collections.singletonList(accountEntity));

        // when
        final List<TransactionalAccount> result =
                laBanquePostaleAccountConverter.toTinkAccounts(accountResponse);

        // then
        assertThat(result).hasSize(1);

        final TransactionalAccount resultAccount = result.get(0);
        assertThat(resultAccount.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(resultAccount.getExactBalance().getExactValue().toString())
                .isEqualTo(clbdBalance.getBalanceAmount().toAmount().getExactValue().toString());
        assertThat(resultAccount.getExactBalance().getCurrencyCode())
                .isEqualTo(clbdBalance.getBalanceAmount().getCurrency());
        assertThat(resultAccount.getApiIdentifier()).isEqualTo(RESOURCE_ID);
        assertThat(resultAccount.getIdModule().getAccountName()).isEqualTo(NAME);
        assertThat(resultAccount.getIdModule().getAccountNumber()).isEqualTo(IBAN);
        assertThat(resultAccount.getIdModule().getUniqueId()).isEqualTo(IBAN);
    }

    @Test
    public void shouldThrowExceptionWhenNoBalancePresent() {
        // given
        final AccountEntity accountEntity = createAccountEntityMock();
        final List<BalanceBaseEntity> balances = Collections.emptyList();

        when(accountEntity.getBalances()).thenReturn(balances);

        final AccountResponse accountResponse = mock(AccountResponse.class);

        when(accountResponse.getAccounts()).thenReturn(ImmutableList.of(accountEntity));

        // when
        final Throwable thrown =
                catchThrowable(
                        () -> laBanquePostaleAccountConverter.toTinkAccounts(accountResponse));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot determine booked balance from empty list of balances.");
    }

    private static AccountEntity createAccountEntityMock() {
        final AccountIdentificationDto accountIdentificationDto =
                mock(AccountIdentificationDto.class);
        when(accountIdentificationDto.getCurrency()).thenReturn(CURRENCY);
        when(accountIdentificationDto.getIban()).thenReturn(IBAN);

        final AccountEntity accountEntity = mock(AccountEntity.class);
        when(accountEntity.getAccountId()).thenReturn(accountIdentificationDto);
        when(accountEntity.getAccountNumber()).thenReturn(IBAN);
        when(accountEntity.getName()).thenReturn(NAME);
        when(accountEntity.getResourceId()).thenReturn(RESOURCE_ID);
        when(accountEntity.getCashAccountType()).thenReturn(CashAccountType.CACC);
        when(accountEntity.getUniqueIdentifier()).thenReturn(IBAN);
        when(accountEntity.getIdentifier()).thenReturn(new IbanIdentifier(IBAN));

        return accountEntity;
    }

    private static BalanceBaseEntity createBalanceBaseEntity(String balanceType, String amount) {
        final BalanceAmountBaseEntity balanceAmountBaseEntity = mock(BalanceAmountBaseEntity.class);
        when(balanceAmountBaseEntity.getCurrency()).thenReturn(CURRENCY);
        when(balanceAmountBaseEntity.toAmount())
                .thenReturn(new ExactCurrencyAmount(new BigDecimal(amount), CURRENCY));

        final BalanceBaseEntity balanceBaseEntity = mock(BalanceBaseEntity.class);
        when(balanceBaseEntity.getBalanceAmount()).thenReturn(balanceAmountBaseEntity);
        when(balanceBaseEntity.getBalanceType()).thenReturn(balanceType);
        when(balanceBaseEntity.isInCurrency(CURRENCY)).thenReturn(Boolean.TRUE);
        when(balanceBaseEntity.toAmount())
                .thenReturn(new ExactCurrencyAmount(new BigDecimal(amount), CURRENCY));

        return balanceBaseEntity;
    }
}
