package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.StarlingTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountHolderNameResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountIdentifiersResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.StarlingAccountHolderType;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class StarlingTransactionalAccountFetcherTest {

    static final String ACCOUNTS_ENTITY =
            "{\"accounts\": [{ \"accountUid\": \"7127e07e-9e9b-4426-8264-2945913fbda8\",\"accountType\": \"PRIMARY\",\"defaultCategory\": \"6e946cde-ba77-43ad-8460-9c3c04d40490\",\"currency\": \"GBP\",\"createdAt\": \"2017-05-15T12:00:00.000Z\",\"name\": \"Personal\"}]}";
    static final String EMPTY_ACCOUNTS_ENTITY = "{\"accounts\": []}";
    static final String ACCOUNT_IDENTIFIER =
            "{\"accountIdentifier\": \"41062849\",\"bankIdentifier\": \"608371\",\"iban\": \"GB80SRLG60837141062849\",\"bic\": \"SRLGGB2L\",\"accountIdentifiers\": [{\"identifierType\": \"IBAN_BIC\",\"bankIdentifier\": \"SRLGGB2L\",\"accountIdentifier\": \"GB80SRLG60837141062849\"},{\"identifierType\": \"SORT_CODE\",\"bankIdentifier\": \"608371\",\"accountIdentifier\": \"41062849\"}]}";
    static final String FULL_ACCOUNT_BALANCE_ENTITY =
            "{\"clearedBalance\": {\"currency\": \"GBP\",\"minorUnits\": 123456},\"effectiveBalance\": {\"currency\": \"GBP\",\"minorUnits\": 123456},\"pendingTransactions\": {\"currency\": \"GBP\",\"minorUnits\": 123456},\"acceptedOverdraft\": { \"currency\": \"GBP\",\"minorUnits\": 123456},\"amount\": {\"currency\": \"GBP\",\"minorUnits\": 123456}}";
    static final String ACCOUNT_BALANCE_WITHOUT_EFFECTIVE_BALANCE_ENTITY =
            "{\"clearedBalance\": {\"currency\": \"GBP\",\"minorUnits\": 123456},\"pendingTransactions\": {\"currency\": \"GBP\",\"minorUnits\": 123456},\"acceptedOverdraft\": { \"currency\": \"GBP\",\"minorUnits\": 123456},\"amount\": {\"currency\": \"GBP\",\"minorUnits\": 123456}}";
    static final String ACCOUNT_BALANCE_WITHOUT_CLEARED_BALANCE_ENTITY =
            "{\"effectiveBalance\": {\"currency\": \"GBP\",\"minorUnits\": 123456},\"pendingTransactions\": {\"currency\": \"GBP\",\"minorUnits\": 123456},\"acceptedOverdraft\": { \"currency\": \"GBP\",\"minorUnits\": 123456},\"amount\": {\"currency\": \"GBP\",\"minorUnits\": 123456}}";

    private StarlingApiClient apiClient;
    private StarlingTransactionalAccountFetcher transactionalAccountFetcher;

    @Before
    public void setup() {
        apiClient = mock(StarlingApiClient.class);
        transactionalAccountFetcher = new StarlingTransactionalAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchAndMapAccounts() {
        // given
        constructAccount(FULL_ACCOUNT_BALANCE_ENTITY);

        // when
        final Collection<TransactionalAccount> transactionalAccounts =
                transactionalAccountFetcher.fetchAccounts();

        // then
        assertEquals(1, transactionalAccounts.size());
    }

    @Test
    public void shouldHandleEmptyAccounts() {
        // given
        AccountsResponse accountEntities =
                SerializationUtils.deserializeFromString(
                        EMPTY_ACCOUNTS_ENTITY, AccountsResponse.class);
        when(apiClient.fetchAccounts()).thenReturn(accountEntities.getAccounts());

        // when
        final Collection<TransactionalAccount> transactionalAccounts =
                transactionalAccountFetcher.fetchAccounts();

        // then
        assertEquals(0, transactionalAccounts.size());
    }

    @Test
    public void shouldMapFullBalance() {
        // given
        constructAccount(FULL_ACCOUNT_BALANCE_ENTITY);

        // when
        final Collection<TransactionalAccount> transactionalAccounts =
                transactionalAccountFetcher.fetchAccounts();

        // then
        assertThat(transactionalAccounts.stream().findFirst().get().getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of("1234.56", "GBP"));
        assertThat(transactionalAccounts.stream().findFirst().get().getExactAvailableBalance())
                .isEqualTo(ExactCurrencyAmount.of("1234.56", "GBP"));
    }

    @Test
    public void shouldMapBalanceWithoutAvailableBalance() {
        // given
        constructAccount(ACCOUNT_BALANCE_WITHOUT_EFFECTIVE_BALANCE_ENTITY);

        // when
        final Collection<TransactionalAccount> transactionalAccounts =
                transactionalAccountFetcher.fetchAccounts();

        // then
        assertThat(transactionalAccounts.stream().findFirst().get().getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of("1234.56", "GBP"));
        assertNull(transactionalAccounts.stream().findFirst().get().getExactAvailableBalance());
    }

    @Test
    public void shouldThrowExceptionWhenExactBalanceIsMissing() {
        // given
        constructAccount(ACCOUNT_BALANCE_WITHOUT_CLEARED_BALANCE_ENTITY);

        // when
        Throwable throwable = catchThrowable(() -> transactionalAccountFetcher.fetchAccounts());

        // then
        assertThat(throwable)
                .isExactlyInstanceOf(AccountRefreshException.class)
                .hasMessage("Balance cannot be found.");
    }

    private void constructAccount(String accountBalance) {
        AccountsResponse accountEntities =
                SerializationUtils.deserializeFromString(
                        StarlingTransactionalAccountFetcherTest.ACCOUNTS_ENTITY,
                        AccountsResponse.class);
        StarlingAccountHolderType starlingAccountHolderType = StarlingAccountHolderType.INDIVIDUAL;
        AccountHolderNameResponse holderNameResponse = new AccountHolderNameResponse("Marcin");
        AccountIdentifiersResponse accountIdentifiersResponse =
                SerializationUtils.deserializeFromString(
                        StarlingTransactionalAccountFetcherTest.ACCOUNT_IDENTIFIER,
                        AccountIdentifiersResponse.class);
        AccountBalanceResponse accountBalanceResponse =
                SerializationUtils.deserializeFromString(
                        accountBalance, AccountBalanceResponse.class);

        when(apiClient.fetchAccounts()).thenReturn(accountEntities.getAccounts());
        when(apiClient.fetchAccountHolderType()).thenReturn(starlingAccountHolderType);
        when(apiClient.fetchAccountIdentifiers(any())).thenReturn(accountIdentifiersResponse);
        when(apiClient.fetchAccountBalance(any())).thenReturn(accountBalanceResponse);
        when(apiClient.fetchAccountHolderName()).thenReturn(holderNameResponse);
    }
}
