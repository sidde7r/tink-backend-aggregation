package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.AMOUNT_1;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.AMOUNT_2;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.IBAN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.NAME;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.RESOURCE_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcherTestFixtures.CURRENCY;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.dto.AccountResourceDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.dto.BalanceResourceDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.AmountTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.BalanceStatusEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PsuAccountIdentificationEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class CmcicTransactionalAccountConverterTest {

    private CmcicTransactionalAccountConverter cmcicTransactionalAccountConverter;

    private PrioritizedValueExtractor prioritizedValueExtractorMock;

    @Before
    public void setUp() {
        prioritizedValueExtractorMock = mock(PrioritizedValueExtractor.class);

        cmcicTransactionalAccountConverter =
                new CmcicTransactionalAccountConverter(prioritizedValueExtractorMock);
    }

    @Test
    public void shouldConvertAccountResourceForTwoBalances() {
        // given
        final AccountResourceDto accountResourceDto = createAccountResourceDtoMock();
        final BalanceResourceDto clbdBalance =
                createBalanceResourceDto(BalanceStatusEntity.CLBD, AMOUNT_1);
        final BalanceResourceDto xpcdBalance =
                createBalanceResourceDto(BalanceStatusEntity.XPCD, AMOUNT_2);
        final List<BalanceResourceDto> balances = ImmutableList.of(clbdBalance, xpcdBalance);

        when(accountResourceDto.getBalances()).thenReturn(balances);

        when(prioritizedValueExtractorMock.pickByValuePriority(any(), any(), any()))
                .thenReturn(Optional.of(xpcdBalance));

        // when
        final Optional<TransactionalAccount> result =
                cmcicTransactionalAccountConverter.convertAccountResourceToTinkAccount(
                        accountResourceDto);

        // then
        assertThat(result.isPresent()).isTrue();
        result.ifPresent(
                transactionalAccount -> {
                    assertThat(transactionalAccount.getType()).isEqualTo(AccountTypes.CHECKING);
                    assertThat(transactionalAccount.getExactBalance().getExactValue().toString())
                            .isEqualTo(xpcdBalance.getBalanceAmount().getAmount());
                    assertThat(transactionalAccount.getExactBalance().getCurrencyCode())
                            .isEqualTo(xpcdBalance.getBalanceAmount().getCurrency());
                    assertThat(transactionalAccount.getApiIdentifier()).isEqualTo(RESOURCE_ID);
                    assertThat(transactionalAccount.getIdModule().getAccountName()).isEqualTo(NAME);
                    assertThat(transactionalAccount.getIdModule().getAccountNumber())
                            .isEqualTo(IBAN);
                    assertThat(transactionalAccount.getIdModule().getUniqueId()).isEqualTo(IBAN);
                });
    }

    @Test
    public void shouldThrowExceptionWhenNoBalancePresent() {
        // given
        final AccountResourceDto accountResourceDto = createAccountResourceDtoMock();
        final BalanceResourceDto otherBalance =
                createBalanceResourceDto(BalanceStatusEntity.OTHR, AMOUNT_1);
        final List<BalanceResourceDto> balances = ImmutableList.of(otherBalance);

        when(accountResourceDto.getBalances()).thenReturn(balances);

        when(prioritizedValueExtractorMock.pickByValuePriority(any(), any(), any()))
                .thenReturn(Optional.empty());

        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                cmcicTransactionalAccountConverter
                                        .convertAccountResourceToTinkAccount(accountResourceDto));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(NoSuchElementException.class)
                .hasMessage(
                        "Could not extract account balance. No available balance with type of: XPCD, CLBD");
    }

    private static AccountResourceDto createAccountResourceDtoMock() {
        final PsuAccountIdentificationEntity psuAccountIdentificationEntityMock =
                mock(PsuAccountIdentificationEntity.class);
        when(psuAccountIdentificationEntityMock.getIban()).thenReturn(IBAN);

        final AccountResourceDto accountResourceDtoMock = mock(AccountResourceDto.class);

        when(accountResourceDtoMock.getAccountId()).thenReturn(psuAccountIdentificationEntityMock);
        when(accountResourceDtoMock.getName()).thenReturn(NAME);
        when(accountResourceDtoMock.getResourceId()).thenReturn(RESOURCE_ID);

        return accountResourceDtoMock;
    }

    private static BalanceResourceDto createBalanceResourceDto(
            BalanceStatusEntity balanceStatusEntity, String amount) {
        final BalanceResourceDto balanceResourceDtoMock = mock(BalanceResourceDto.class);

        final AmountTypeEntity amountTypeEntityMock = mock(AmountTypeEntity.class);
        when(amountTypeEntityMock.getAmount()).thenReturn(amount);
        when(amountTypeEntityMock.getCurrency()).thenReturn(CURRENCY);

        when(balanceResourceDtoMock.getBalanceAmount()).thenReturn(amountTypeEntityMock);
        when(balanceResourceDtoMock.getBalanceType()).thenReturn(balanceStatusEntity);

        return balanceResourceDtoMock;
    }
}
