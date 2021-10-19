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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceAmountBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.CashAccountType;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class LaBanquePostaleTransactionalAccountConverterTest {

    private LaBanquePostaleTransactionalAccountConverter transactionalAccountConverter;

    @Before
    public void setUp() {

        transactionalAccountConverter = new LaBanquePostaleTransactionalAccountConverter();
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

        // when

        Optional<TransactionalAccount> accountOptional =
                transactionalAccountConverter.toTransactionalAccount(accountEntity);

        // then
        assertThat(accountOptional).isNotEmpty();
        TransactionalAccount account = accountOptional.get();
        assertThat(account.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(account.getExactBalance().getExactValue().toString())
                .isEqualTo(clbdBalance.getBalanceAmount().toAmount().getExactValue().toString());
        assertThat(account.getExactBalance().getCurrencyCode())
                .isEqualTo(clbdBalance.getBalanceAmount().getCurrency());
        assertThat(account.getApiIdentifier()).isEqualTo(RESOURCE_ID);
        IdModule idModule = account.getIdModule();
        assertThat(idModule.getAccountName()).isEqualTo(NAME);
        assertThat(idModule.getAccountNumber()).isEqualTo(IBAN);
        assertThat(idModule.getUniqueId()).isEqualTo(IBAN);
    }

    @Test
    public void shouldThrowExceptionWhenNoBalancePresent() {
        // given
        final AccountEntity accountEntity = createAccountEntityMock();
        final List<BalanceBaseEntity> balances = Collections.emptyList();

        when(accountEntity.getBalances()).thenReturn(balances);

        // when
        final Throwable thrown =
                catchThrowable(
                        () -> transactionalAccountConverter.toTransactionalAccount(accountEntity));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot determine booked balance from empty list of balances.");
    }

    private static AccountEntity createAccountEntityMock() {
        final AccountIdentificationEntity accountIdentificationDto =
                mock(AccountIdentificationEntity.class);
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
        when(balanceBaseEntity.getBalanceType())
                .thenReturn(BalanceType.findByStringType(balanceType));
        when(balanceBaseEntity.isInCurrency(CURRENCY)).thenReturn(Boolean.TRUE);
        when(balanceBaseEntity.toTinkAmount())
                .thenReturn(new ExactCurrencyAmount(new BigDecimal(amount), CURRENCY));

        return balanceBaseEntity;
    }
}
