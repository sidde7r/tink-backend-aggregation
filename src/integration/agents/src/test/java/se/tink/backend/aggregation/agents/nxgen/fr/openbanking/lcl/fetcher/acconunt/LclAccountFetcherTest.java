package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fetcher.acconunt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.IBAN;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.RESOURCE_ID;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createAccountResourceDtoMock;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createAccountsResponseDto;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createBalanceResourceDtoMock;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createExactCurrencyAmount;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createLclDataConverterMock;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.AccountResourceDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.AccountsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.BalanceResourceDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.BalanceType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.CashAccountType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.account.LclAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.converter.LclDataConverter;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class LclAccountFetcherTest {

    private LclAccountFetcher lclAccountFetcher;

    private LclApiClient apiClientMock;

    private PrioritizedValueExtractor prioritizedValueExtractorMock;

    @Before
    public void setUp() {
        final LclDataConverter dataConverterMock = createLclDataConverterMock();

        apiClientMock = mock(LclApiClient.class);
        prioritizedValueExtractorMock = mock(PrioritizedValueExtractor.class);

        lclAccountFetcher =
                new LclAccountFetcher(
                        apiClientMock, prioritizedValueExtractorMock, dataConverterMock);
    }

    @Test
    public void shouldFetchAccountsForOneCaccAccount() {
        // given
        final AccountsResponseDto accountsResponseDtoMock = createAccountsResponseDto();
        final BalanceResourceDto clbdBalance = createBalanceResourceDtoMock(BalanceType.CLBD);
        setUpAccountsResponseForCaccAccount(accountsResponseDtoMock, ImmutableList.of(clbdBalance));

        when(apiClientMock.getAccountsResponse()).thenReturn(accountsResponseDtoMock);

        when(prioritizedValueExtractorMock.pickByValuePriority(any(), any(), any()))
                .thenReturn(Optional.of(clbdBalance));

        // when
        final List<TransactionalAccount> returnedResult = lclAccountFetcher.fetchAccounts();

        // then
        assertThat(returnedResult).hasSize(1);

        final TransactionalAccount returnedTransactionalAccount = returnedResult.get(0);
        assertThat(returnedTransactionalAccount.getType()).isEqualTo(AccountTypes.CHECKING);

        final ExactCurrencyAmount expectedExactCurrencyAmount = createExactCurrencyAmount();
        assertThat(returnedTransactionalAccount.getExactBalance())
                .isEqualTo(expectedExactCurrencyAmount);

        assertThat(returnedTransactionalAccount.getAccountNumber()).isEqualTo(IBAN);
        assertThat(returnedTransactionalAccount.getApiIdentifier()).isEqualTo(RESOURCE_ID);

        assertThat(returnedTransactionalAccount.getIdModule().getUniqueId()).isEqualTo(IBAN);
    }

    @Test
    public void shouldFetchNoAccountsForOneCardAccount() {
        // given
        final AccountsResponseDto accountsResponseDtoMock = createAccountsResponseDto();
        setUpAccountsResponseForCardAccount(accountsResponseDtoMock, Collections.emptyList());

        when(apiClientMock.getAccountsResponse()).thenReturn(accountsResponseDtoMock);

        // when
        final List<TransactionalAccount> returnedResult = lclAccountFetcher.fetchAccounts();

        // then
        assertThat(returnedResult).isEmpty();
    }

    @Test
    public void shouldFetchAccountsForAccountWithTwoBalances() {
        // given
        final AccountsResponseDto accountsResponseDtoMock = createAccountsResponseDto();
        final BalanceResourceDto clbdBalance = createBalanceResourceDtoMock(BalanceType.CLBD);
        final BalanceResourceDto xpcdBalance = createBalanceResourceDtoMock(BalanceType.XPCD);
        setUpAccountsResponseForCaccAccount(
                accountsResponseDtoMock, ImmutableList.of(clbdBalance, xpcdBalance));

        when(apiClientMock.getAccountsResponse()).thenReturn(accountsResponseDtoMock);

        when(prioritizedValueExtractorMock.pickByValuePriority(any(), any(), any()))
                .thenReturn(Optional.of(clbdBalance));

        // when
        final List<TransactionalAccount> returnedResult = lclAccountFetcher.fetchAccounts();

        // then
        assertThat(returnedResult).hasSize(1);

        final TransactionalAccount returnedTransactionalAccount = returnedResult.get(0);
        assertThat(returnedTransactionalAccount.getType()).isEqualTo(AccountTypes.CHECKING);

        final ExactCurrencyAmount expectedExactCurrencyAmount = createExactCurrencyAmount();
        assertThat(returnedTransactionalAccount.getExactBalance())
                .isEqualTo(expectedExactCurrencyAmount);

        assertThat(returnedTransactionalAccount.getAccountNumber()).isEqualTo(IBAN);
        assertThat(returnedTransactionalAccount.getApiIdentifier()).isEqualTo(RESOURCE_ID);

        assertThat(returnedTransactionalAccount.getIdModule().getUniqueId()).isEqualTo(IBAN);
    }

    @Test
    public void shouldThrowExceptionWhenNoBalancePresent() {
        // given
        final AccountsResponseDto accountsResponseDtoMock = createAccountsResponseDto();
        final BalanceResourceDto otherBalance = createBalanceResourceDtoMock(BalanceType.OTHR);
        setUpAccountsResponseForCaccAccount(
                accountsResponseDtoMock, ImmutableList.of(otherBalance));

        when(apiClientMock.getAccountsResponse()).thenReturn(accountsResponseDtoMock);

        when(prioritizedValueExtractorMock.pickByValuePriority(any(), any(), any()))
                .thenReturn(Optional.empty());

        // when
        final Throwable thrown = catchThrowable(lclAccountFetcher::fetchAccounts);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(AccountRefreshException.class)
                .hasMessage(
                        "Could not extract account balance. No available balance with type of: CLBD, XPCD");
    }

    private static void setUpAccountsResponseForCaccAccount(
            AccountsResponseDto accountsResponseDtoMock, List<BalanceResourceDto> balances) {
        final AccountResourceDto accountResourceDtoMock =
                createAccountResourceDtoMock(CashAccountType.CACC, balances);
        when(accountsResponseDtoMock.getAccounts())
                .thenReturn(ImmutableList.of(accountResourceDtoMock));
    }

    private static void setUpAccountsResponseForCardAccount(
            AccountsResponseDto accountsResponseDtoMock, List<BalanceResourceDto> balances) {
        final AccountResourceDto accountResourceDtoMock =
                createAccountResourceDtoMock(CashAccountType.CARD, balances);
        when(accountsResponseDtoMock.getAccounts())
                .thenReturn(ImmutableList.of(accountResourceDtoMock));
    }
}
