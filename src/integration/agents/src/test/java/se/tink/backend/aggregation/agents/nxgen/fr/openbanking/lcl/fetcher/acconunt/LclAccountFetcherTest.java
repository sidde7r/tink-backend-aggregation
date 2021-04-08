package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fetcher.acconunt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
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

public class LclAccountFetcherTest {

    private LclAccountFetcher lclAccountFetcher;

    private LclApiClient apiClientMock;

    @Before
    public void setUp() {
        final LclDataConverter dataConverterMock = createLclDataConverterMock();

        apiClientMock = mock(LclApiClient.class);

        lclAccountFetcher = new LclAccountFetcher(apiClientMock, dataConverterMock);
    }

    @Test
    public void shouldFetchAccountsForOneCaccAccount() {
        // given
        final AccountsResponseDto accountsResponseDtoMock = createAccountsResponseDto();
        final BalanceResourceDto clbdBalance = createBalanceResourceDtoMock(BalanceType.CLBD);
        setUpAccountsResponseForCaccAccount(accountsResponseDtoMock, Arrays.asList(clbdBalance));

        when(apiClientMock.getAccountsResponse()).thenReturn(accountsResponseDtoMock);

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
                accountsResponseDtoMock, Arrays.asList(clbdBalance, xpcdBalance));

        when(apiClientMock.getAccountsResponse()).thenReturn(accountsResponseDtoMock);

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
        setUpAccountsResponseForCaccAccount(accountsResponseDtoMock, Collections.emptyList());

        when(apiClientMock.getAccountsResponse()).thenReturn(accountsResponseDtoMock);

        // when
        final Throwable thrown = catchThrowable(lclAccountFetcher::fetchAccounts);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot determine booked balance from empty list of balances.");
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
