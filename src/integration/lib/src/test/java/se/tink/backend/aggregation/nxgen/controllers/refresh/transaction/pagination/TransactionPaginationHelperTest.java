package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.credentials.service.AccountTransactionRefreshScope;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.TransactionRefreshScope;

@RunWith(MockitoJUnitRunner.class)
public class TransactionPaginationHelperTest {

    @Mock private Account account;

    @Test
    public void
            ensure_getContentWithRefreshDate_returnsProperRefreshDate_whenIsDefinedOnTransactionScope() {
        // given
        RefreshScope refreshScope = new RefreshScope();
        TransactionRefreshScope transactionRefreshScope = new TransactionRefreshScope();
        LocalDate expectedLocalDate = LocalDate.parse("2021-06-04");
        transactionRefreshScope.setTransactionBookedDateGte(expectedLocalDate);
        refreshScope.setTransactions(transactionRefreshScope);
        TransactionPaginationHelper helper = new TransactionPaginationHelper(refreshScope);

        when(account.getIdentifiers())
                .thenReturn(
                        Collections.singletonList(
                                AccountIdentifier.createOrThrow(
                                        "tink://720fb05a-c01b-47b7-baa1-5da02e165d1e")));

        // when
        Optional<Date> date = helper.getContentWithRefreshDate(account);

        // then
        assertTrue(date.isPresent());
        assertEquals(
                expectedLocalDate.atStartOfDay().toInstant(ZoneOffset.UTC), date.get().toInstant());
    }

    @Test
    public void
            ensure_getContentWithRefreshDate_returnsProperRefreshDate_whenIsDefinedOnAccountTransactionScope() {
        // given
        RefreshScope refreshScope = new RefreshScope();
        TransactionRefreshScope transactionRefreshScope = new TransactionRefreshScope();
        refreshScope.setTransactions(transactionRefreshScope);
        AccountTransactionRefreshScope accountTransactionRefreshScope =
                new AccountTransactionRefreshScope();
        String accountIdentifier = "tink://720fb05a-c01b-47b7-baa1-5da02e165d1e";
        accountTransactionRefreshScope.setAccountIdentifiers(
                Collections.singleton(accountIdentifier));
        LocalDate expectedLocalDate = LocalDate.parse("2021-06-04");
        accountTransactionRefreshScope.setTransactionBookedDateGte(expectedLocalDate);
        transactionRefreshScope.setAccounts(Collections.singleton(accountTransactionRefreshScope));
        TransactionPaginationHelper helper = new TransactionPaginationHelper(refreshScope);

        when(account.getIdentifiers())
                .thenReturn(
                        Collections.singletonList(
                                AccountIdentifier.createOrThrow(accountIdentifier)));

        // when
        Optional<Date> date = helper.getContentWithRefreshDate(account);

        // then
        assertTrue(date.isPresent());
        assertEquals(
                expectedLocalDate.atStartOfDay().toInstant(ZoneOffset.UTC), date.get().toInstant());
    }

    @Test
    public void
            ensure_getContentWithRefreshDate_returnsProperRefreshDate_whenIsDefinedOnBothTransactionAndAccountTransactionScope() {
        // given
        RefreshScope refreshScope = new RefreshScope();
        TransactionRefreshScope transactionRefreshScope = new TransactionRefreshScope();
        transactionRefreshScope.setTransactionBookedDateGte(LocalDate.parse("2021-06-03"));
        refreshScope.setTransactions(transactionRefreshScope);
        AccountTransactionRefreshScope accountTransactionRefreshScope =
                new AccountTransactionRefreshScope();
        String accountIdentifier = "tink://720fb05a-c01b-47b7-baa1-5da02e165d1e";
        accountTransactionRefreshScope.setAccountIdentifiers(
                Collections.singleton(accountIdentifier));
        LocalDate expectedLocalDate = LocalDate.parse("2021-06-04");
        accountTransactionRefreshScope.setTransactionBookedDateGte(expectedLocalDate);
        transactionRefreshScope.setAccounts(Collections.singleton(accountTransactionRefreshScope));
        TransactionPaginationHelper helper = new TransactionPaginationHelper(refreshScope);

        when(account.getIdentifiers())
                .thenReturn(
                        Collections.singletonList(
                                AccountIdentifier.createOrThrow(accountIdentifier)));

        // when
        Optional<Date> date = helper.getContentWithRefreshDate(account);

        // then
        assertTrue(date.isPresent());
        assertEquals(
                expectedLocalDate.atStartOfDay().toInstant(ZoneOffset.UTC), date.get().toInstant());
    }

    @Test
    public void ensure_getContentWithRefreshDate_returnsEmptyRefreshDate_whenRefreshScopeIsNull() {
        // given
        TransactionPaginationHelper helper = new TransactionPaginationHelper((RefreshScope) null);

        String accountIdentifier = "tink://720fb05a-c01b-47b7-baa1-5da02e165d1e";
        when(account.getIdentifiers())
                .thenReturn(
                        Collections.singletonList(
                                AccountIdentifier.createOrThrow(accountIdentifier)));

        // when
        Optional<Date> date = helper.getContentWithRefreshDate(account);

        // then
        assertFalse(date.isPresent());
    }

    @Test
    public void
            ensure_getContentWithRefreshDate_returnsEmptyRefreshDate_whenTransactionRefreshScopeIsNull() {
        // given
        RefreshScope refreshScope = new RefreshScope();
        TransactionPaginationHelper helper = new TransactionPaginationHelper(refreshScope);

        String accountIdentifier = "tink://720fb05a-c01b-47b7-baa1-5da02e165d1e";
        when(account.getIdentifiers())
                .thenReturn(
                        Collections.singletonList(
                                AccountIdentifier.createOrThrow(accountIdentifier)));

        // when
        Optional<Date> date = helper.getContentWithRefreshDate(account);

        // then
        assertFalse(date.isPresent());
    }
}
