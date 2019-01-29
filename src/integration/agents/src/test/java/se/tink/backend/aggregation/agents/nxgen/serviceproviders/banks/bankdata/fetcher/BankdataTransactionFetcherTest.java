package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataTestConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.BankdataPinAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

import java.util.Collection;

public class BankdataTransactionFetcherTest {
    private boolean debugOutput;
    private String bankNo;
    private String username;
    private String password;
    private BankdataApiClient bankClient;

    @Before
    public void setUp() throws Exception {
        username = BankdataTestConfig.USERNAME;
        password = BankdataTestConfig.PASSWORD;
        bankNo = BankdataTestConfig.BANK_NO;
        debugOutput = BankdataTestConfig.DEBUG_OUTPUT;

        Credentials credentials = new Credentials();
        AgentContext context = new AgentTestContext(credentials);
        TinkHttpClient client = new TinkHttpClient(context.getAggregatorInfo(), context.getMetricRegistry(),
                context.getLogOutputStream(), null, null);
        client.setDebugOutput(debugOutput);
        Provider provider = new Provider();
        provider.setPayload(bankNo);
        bankClient = new BankdataApiClient(client, provider);
    }

    @Test
    public void fetchTransactions() throws Exception {
        BankdataPinAuthenticator authenticator = new BankdataPinAuthenticator(bankClient);
        BankdataTransactionalAccountFetcher accountFetcher = new BankdataTransactionalAccountFetcher(bankClient);
        BankdataTransactionFetcher transactionFetcher = new BankdataTransactionFetcher(bankClient);
        authenticator.authenticate(username, password);
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        for (TransactionalAccount account : accounts) {
            int startPage = 0;
            boolean fetchmore = false;
            do {
                System.out.println("Account: " + account.getName());
                PaginatorResponse response = transactionFetcher.getTransactionsFor(account, startPage);
                for (Transaction transaction : response.getTinkTransactions()) {
                    System.out.println("Transaction: " + transaction.getDescription());
                }
                fetchmore = response.canFetchMore().get();
            } while (fetchmore);

        }
    }
}
