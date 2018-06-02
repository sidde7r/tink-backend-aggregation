package se.tink.backend.common.search;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.common.ServiceContext;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.core.SearchResult;
import se.tink.backend.core.Transaction;
import se.tink.libraries.date.DateUtils;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionsSearcherTest {

    private TransactionsSearcher transactionsSearcher;

    private final String CREDENTIALS_ID_1 = "testCredential1";
    private final String CREDENTIALS_ID_2 = "testCredential2";

    @Before
    public void setup() {
        transactionsSearcher = createTransactionSearcher();
    }

    private TransactionsSearcher createTransactionSearcher() {
        ServiceContext serviceContext = mock(ServiceContext.class);
        when(serviceContext.getRepository(AccountRepository.class))
                .thenReturn(mock(AccountRepository.class));
        when(serviceContext.getRepository(CategoryRepository.class))
                .thenReturn(mock(CategoryRepository.class));
        when(serviceContext.getRepository(CredentialsRepository.class))
                .thenReturn(mock(CredentialsRepository.class));

        return new TransactionsSearcher(serviceContext, new MetricRegistry());
    }

    @Test
    public void excludeUpcomingTransactions() throws InterruptedException {
        Map<String, Date> credentialsIdByUpdatedDate = ImmutableMap
                .of(CREDENTIALS_ID_1, DateUtils.parseDate("2016-05-25"),
                        CREDENTIALS_ID_2, DateUtils.parseDate("2016-05-27"));

        List<SearchResult> testSearchResult = Arrays
                .asList(createSearchResult(2016, Calendar.MARCH, 15, CREDENTIALS_ID_1),
                        createSearchResult(2016, Calendar.MAY, 26, CREDENTIALS_ID_1),
                        createSearchResult(2016, Calendar.MAY, 26, CREDENTIALS_ID_2),
                        createSearchResult(2016, Calendar.MAY, 28, CREDENTIALS_ID_2));

        List<SearchResult> results = transactionsSearcher
                .excludeUpcomingTransactions(testSearchResult, credentialsIdByUpdatedDate);
        assertEquals(results.size(), 2);
    }

    @Test
    public void excludeTransactionsForNonUpdatedCredentials() {
        Map<String, Date> credentialsIdByUpdatedDate = Maps.newHashMap();
        credentialsIdByUpdatedDate.put(CREDENTIALS_ID_1, DateUtils.parseDate("2016-05-25"));
        credentialsIdByUpdatedDate.put(CREDENTIALS_ID_2, null);

        List<SearchResult> testSearchResult = Arrays
                .asList(createSearchResult(2016, Calendar.MARCH, 15, CREDENTIALS_ID_2),
                        createSearchResult(2016, Calendar.MARCH, 16, CREDENTIALS_ID_2),
                        createSearchResult(2016, Calendar.MARCH, 17, CREDENTIALS_ID_1));

        List<SearchResult> results = transactionsSearcher
                .excludeUpcomingTransactions(testSearchResult, credentialsIdByUpdatedDate);
        assertEquals(results.size(), 1);
        assertEquals(CREDENTIALS_ID_1, results.get(0).getTransaction().getCredentialsId());
    }

    @Test
    public void paginateAndSetUpcoming() {
        Map<String, Date> credentialsIdByUpdatedDate = ImmutableMap
                .of(CREDENTIALS_ID_1, DateUtils.parseDate("2016-05-25"),
                        CREDENTIALS_ID_2, DateUtils.parseDate("2016-05-27"));

        List<SearchResult> testSearchResult = Arrays
                .asList(createSearchResult(2016, Calendar.MARCH, 15, CREDENTIALS_ID_1),
                        createSearchResult(2016, Calendar.MAY, 26, CREDENTIALS_ID_1),
                        createSearchResult(2016, Calendar.MAY, 26, CREDENTIALS_ID_2),
                        createSearchResult(2016, Calendar.MAY, 28, CREDENTIALS_ID_2));

        List<SearchResult> results = transactionsSearcher
                .paginateAndSetUpcoming(credentialsIdByUpdatedDate, testSearchResult, 50, 0);
        assertEquals(results.get(0).getTransaction().isUpcoming(), false);
        assertEquals(results.get(1).getTransaction().isUpcoming(), true);
        assertEquals(results.get(2).getTransaction().isUpcoming(), false);
        assertEquals(results.get(3).getTransaction().isUpcoming(), true);
    }

    /**
     * Helper method for creating a SearchResult with a specific transaction date and credentials id.
     */
    private SearchResult createSearchResult(int year, int month, int date, String credentialsId) {
        SearchResult searchResult = new SearchResult();
        Transaction transaction = new Transaction();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, date);
        transaction.setDate(calendar.getTime());
        transaction.setOriginalDate(transaction.getDate());
        transaction.setCredentialsId(credentialsId);
        searchResult.setTransaction(transaction);
        return searchResult;
    }

}
