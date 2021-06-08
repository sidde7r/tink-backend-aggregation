package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RunWith(MockitoJUnitRunner.class)
public class CertainDateTransactionPaginationHelperTest {

    @Mock private CredentialsRequest request;
    @Mock private Account account;

    @Test
    public void
            ensure_getTransactionDateLimit_returnsProperRefreshDate_whenIsDefinedForAnAccount() {
        // given
        Date expectedDate =
                Date.from(LocalDate.parse("2021-06-04").atStartOfDay().toInstant(ZoneOffset.UTC));

        se.tink.backend.agents.rpc.Account requestAccount =
                mock(se.tink.backend.agents.rpc.Account.class);
        String accountId = "tink://720fb05a-c01b-47b7-baa1-5da02e165d1e";
        when(requestAccount.getBankId()).thenReturn(accountId);
        when(requestAccount.getCertainDate()).thenReturn(expectedDate);
        when(request.getAccounts()).thenReturn(Collections.singletonList(requestAccount));

        when(account.isUniqueIdentifierEqual(eq(accountId))).thenReturn(true);
        TransactionPaginationHelper helper = new CertainDateTransactionPaginationHelper(request);

        // when
        Optional<Date> date = helper.getTransactionDateLimit(account);

        // then
        assertTrue(date.isPresent());
        assertEquals(expectedDate, date.get());
    }

    @Test
    public void
            ensure_getTransactionDateLimit_returnsProperRefreshDate_whenCertainDateIsNullForAnAccount() {
        // given
        se.tink.backend.agents.rpc.Account requestAccount =
                mock(se.tink.backend.agents.rpc.Account.class);
        String accountId = "tink://720fb05a-c01b-47b7-baa1-5da02e165d1e";
        when(requestAccount.getBankId()).thenReturn(accountId);
        when(requestAccount.getCertainDate()).thenReturn(null);
        when(request.getAccounts()).thenReturn(Collections.singletonList(requestAccount));
        when(account.isUniqueIdentifierEqual(eq(accountId))).thenReturn(true);
        TransactionPaginationHelper helper = new CertainDateTransactionPaginationHelper(request);

        // when
        Optional<Date> date = helper.getTransactionDateLimit(account);

        // then
        assertFalse(date.isPresent());
    }

    @Test
    public void
            ensure_getTransactionDateLimit_returnsProperRefreshDate_whenAccountIsNotDefinedInARequest() {
        // given
        se.tink.backend.agents.rpc.Account requestAccount =
                mock(se.tink.backend.agents.rpc.Account.class);
        String accountId = "tink://720fb05a-c01b-47b7-baa1-5da02e165d1e";
        when(requestAccount.getBankId()).thenReturn(accountId);
        when(request.getAccounts()).thenReturn(Collections.singletonList(requestAccount));

        when(account.isUniqueIdentifierEqual(eq(accountId))).thenReturn(false);
        TransactionPaginationHelper helper = new CertainDateTransactionPaginationHelper(request);

        // when
        Optional<Date> date = helper.getTransactionDateLimit(account);

        // then
        assertFalse(date.isPresent());
    }

    @Test
    public void
            ensure_getTransactionDateLimit_returnsProperRefreshDate_whenAccountIsDefinedInARequest() {
        // given
        when(request.getAccounts()).thenReturn(Collections.emptyList());
        TransactionPaginationHelper helper = new CertainDateTransactionPaginationHelper(request);

        // when
        Optional<Date> date = helper.getTransactionDateLimit(account);

        // then
        assertFalse(date.isPresent());
    }

    @Test
    public void
            ensure_getTransactionDateLimit_returnsEmptyRefreshDate_whenAccountsInRequestAreNull() {
        // given
        when(request.getAccounts()).thenReturn(null);
        TransactionPaginationHelper helper = new CertainDateTransactionPaginationHelper(request);

        // when
        Optional<Date> date = helper.getTransactionDateLimit(account);

        // then
        assertFalse(date.isPresent());
    }
}
