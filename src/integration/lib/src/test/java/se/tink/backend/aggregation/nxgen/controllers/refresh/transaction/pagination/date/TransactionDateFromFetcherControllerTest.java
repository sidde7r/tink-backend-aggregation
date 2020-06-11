package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class TransactionDateFromFetcherControllerTest {

    private CredentialsRequest credentialsRequest;
    private List<Account> credentialsRequestAccounts;
    private TransactionalAccount account;
    private TransactionDateFromFetcher<TransactionalAccount> fetcher;
    private TransactionDateFromFetcherController<TransactionalAccount> objectUnderTest;

    @Before
    public void init() {
        credentialsRequest = Mockito.mock(CredentialsRequest.class);
        credentialsRequestAccounts = new LinkedList<>();
        Mockito.when(credentialsRequest.getAccounts()).thenReturn(credentialsRequestAccounts);
        fetcher = Mockito.mock(TransactionDateFromFetcher.class);
        account = Mockito.mock(TransactionalAccount.class);
        objectUnderTest = new TransactionDateFromFetcherController<>(credentialsRequest, fetcher);
    }

    @Test
    public void shouldFetchStartingFromFetcherCalculatedDate() {
        // given
        LocalDate fetcherCalculatedDateFrom = LocalDate.parse("2020-06-01");
        Mockito.when(fetcher.minimalFromDate()).thenReturn(fetcherCalculatedDateFrom);
        List transactions = Lists.newArrayList(Mockito.mock(AggregationTransaction.class));
        Mockito.when(fetcher.fetchTransactionsFor(account, fetcherCalculatedDateFrom))
                .thenReturn(transactions);

        // when
        List<AggregationTransaction> result = objectUnderTest.fetchTransactionsFor(account);

        // then
        Assert.assertTrue(result.size() == 1);
        Assert.assertEquals(transactions.get(0), result.get(0));
    }

    @Test
    public void shouldFetchStartingFromCertainDate() {
        // given
        final String accountId = "1234567890";
        LocalDate fetcherCalculatedDateFrom = LocalDate.parse("2020-05-01");
        LocalDate accountCertainDate = LocalDate.parse("2020-06-01");
        Mockito.when(fetcher.minimalFromDate()).thenReturn(fetcherCalculatedDateFrom);
        List transactions = Lists.newArrayList(Mockito.mock(AggregationTransaction.class));
        Mockito.when(fetcher.fetchTransactionsFor(account, accountCertainDate))
                .thenReturn(transactions);
        Account aggregatedAccount = Mockito.mock(Account.class);
        credentialsRequestAccounts.add(aggregatedAccount);
        Mockito.when(aggregatedAccount.getBankId()).thenReturn(accountId);
        Mockito.when(aggregatedAccount.getCertainDate())
                .thenReturn(
                        Date.from(
                                accountCertainDate
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .toInstant()));
        Mockito.when(account.isUniqueIdentifierEqual(accountId)).thenReturn(true);

        // when
        List<AggregationTransaction> result = objectUnderTest.fetchTransactionsFor(account);

        // then
        Assert.assertTrue(result.size() == 1);
        Assert.assertEquals(transactions.get(0), result.get(0));
    }
}
