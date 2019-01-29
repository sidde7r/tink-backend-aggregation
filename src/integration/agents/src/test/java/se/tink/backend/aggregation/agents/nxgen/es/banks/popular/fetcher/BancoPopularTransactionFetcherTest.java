package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularTestBase;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import static org.junit.Assert.assertNotNull;

public class BancoPopularTransactionFetcherTest extends BancoPopularTestBase {

    private BancoPopularTransactionFetcher transactionFetcher;
    private BancoPopularAccountFetcher accountFetcher;

    @Before
    public void setUp() throws Exception {
        setup();
        accountFetcher = new BancoPopularAccountFetcher(bankClient, persistentStorage);
        transactionFetcher = new BancoPopularTransactionFetcher(bankClient, persistentStorage);
    }

    @Test
    public void getTransactionsFor() throws Exception {
        authenticate();
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();
        Calendar c =Calendar.getInstance();
        Date toDate = c.getTime();
        c.add(Calendar.YEAR, -365);
        Date fromDate = c.getTime();
        for (TransactionalAccount account : accounts) {
            Collection<? extends Transaction> transactions = transactionFetcher
                    .getTransactionsFor(account, fromDate, toDate)
                    .getTinkTransactions();
            assertNotNull(transactions);
            System.out.println("From date " + fromDate);
            System.out.println("To date " + toDate);
            for (Transaction transaction : transactions) {
                assertNotNull(transaction.getDescription());
                assertNotNull(transaction.getDate());
            }
        }
    }

}
