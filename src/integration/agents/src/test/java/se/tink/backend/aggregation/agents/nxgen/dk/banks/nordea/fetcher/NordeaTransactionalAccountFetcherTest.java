package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher;

import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaTestBase;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.TestConfig;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.parser.NordeaDkParser;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.parser.NordeaDkTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.transactionalaccount.NordeaV20TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.parsers.NordeaV20Parser;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class NordeaTransactionalAccountFetcherTest extends NordeaTestBase {
    protected NordeaV20TransactionalAccountFetcher transactionalAccountFetcher;

    @Before
    public void setUp() throws Exception {
        setUpTest();
        NordeaV20Parser parser = new NordeaDkParser(new NordeaDkTransactionParser(), credentials);
        transactionalAccountFetcher = new NordeaV20TransactionalAccountFetcher(bankClient, parser);
    }

    @Test
    public void fetchAccounts() throws Exception {
        authenticateTestUser();
        Collection<TransactionalAccount> accounts = transactionalAccountFetcher.fetchAccounts();
        accounts.forEach(a -> {
            Assert.assertEquals(TestConfig.ACCOUNT_NUMBER, a.getAccountNumber());
        });
    }

    @Test
    public void fetchTransactions() throws Exception {
        authenticateTestUser();
        Collection<TransactionalAccount> accounts = transactionalAccountFetcher.fetchAccounts();
        accounts.forEach(a -> {
            TransactionKeyPaginatorResponse<String> txResponse = transactionalAccountFetcher.getTransactionsFor(a, null);
            Collection<? extends Transaction> transactions = txResponse.getTinkTransactions();

            Assert.assertEquals(1, transactions.size());
            Assert.assertEquals(TestConfig.ACCOUNT_NUMBER, a.getAccountNumber());
        });
    }
}
