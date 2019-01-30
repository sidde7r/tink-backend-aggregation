package se.tink.backend.aggregation.agents.nxgen.fi.banks.op;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.OpAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.OpBankTransactionalAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankTransactionPaginationKey;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankTestConfig.PASSWORD;
import static se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankTestConfig.USERNAME;

public class OpBankFetcherTest {

    private OpBankApiClient bankClient;

    @Before
    public void setUp() throws Exception {
        Credentials credentials = new Credentials();
        credentials.setField(Field.Key.USERNAME, USERNAME);
        credentials.setField(Field.Key.PASSWORD, PASSWORD);
        credentials.setType(CredentialsTypes.PASSWORD);

        AgentContext context = new AgentTestContext(null);
        SupplementalInformationController supplementalInformationController = new SupplementalInformationController(
                context, credentials);
        bankClient = new OpBankApiClient(
                new TinkHttpClient(context.getAggregatorInfo(), context.getMetricRegistry(),
                        context.getLogOutputStream(), null, null)
        );
        OpBankPersistentStorage persistentStorage = new OpBankPersistentStorage(credentials, new PersistentStorage());
        persistentStorage.put(OpBankConstants.Authentication.APPLICATION_INSTANCE_ID, OpBankTestConfig.APPLICATION_INSTANCE_ID);

        OpAutoAuthenticator opBankAuthenticator = new OpAutoAuthenticator(bankClient, persistentStorage,
                credentials);
        opBankAuthenticator.authenticate(USERNAME, PASSWORD);
    }

    @Test
    public void fetchAccountsHappyTrail() throws Exception {
        OpBankTransactionalAccountsFetcher fetcher = new OpBankTransactionalAccountsFetcher(bankClient);
        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        assertTrue("Missing accounts", accounts.size() > 0);
    }

    @Test
    public void fetchTransactions() throws Exception {

        OpBankTransactionalAccountsFetcher fetcher = new OpBankTransactionalAccountsFetcher(bankClient);

        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        for (TransactionalAccount account : accounts) {
            TransactionKeyPaginatorResponse<OpBankTransactionPaginationKey> response = null;
            OpBankTransactionPaginationKey nextKey = null;
            ArrayList<Transaction> transactions = Lists.newArrayList();

            do {
                response = fetcher.getTransactionsFor(account, nextKey);

                nextKey = response.nextKey();
                Collection<? extends Transaction> tx = response.getTinkTransactions();

                transactions.addAll(Optional.ofNullable(tx).orElse(Collections.emptyList()));
           } while (response.canFetchMore().get());

            for (Transaction tinkTransaction : transactions) {
                assertNotNull("Date is null", tinkTransaction.getDate());
            }
        }
    }
}
