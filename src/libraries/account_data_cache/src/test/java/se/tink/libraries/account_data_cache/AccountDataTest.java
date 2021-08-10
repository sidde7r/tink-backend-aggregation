package se.tink.libraries.account_data_cache;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;

public class AccountDataTest {
    private static final String DUMMY_ACCOUNT_ID_0 = "dummy0";

    @Test
    public void testAccountFeatures() {
        AccountData accountData = new AccountData(mockAccount(DUMMY_ACCOUNT_ID_0));

        AccountFeatures loanAccountFeatures = AccountFeatures.createForLoan(new Loan());
        AccountFeatures emptyAccountFeatures = AccountFeatures.createEmpty();

        // Initially empty.
        Assert.assertTrue(accountData.getAccountFeatures().isEmpty());

        accountData.updateAccountFeatures(emptyAccountFeatures);

        // Still empty.
        Assert.assertTrue(accountData.getAccountFeatures().isEmpty());

        accountData.updateAccountFeatures(loanAccountFeatures);

        assertEquals(loanAccountFeatures, accountData.getAccountFeatures());

        accountData.updateAccountFeatures(emptyAccountFeatures);

        // Still loan features, caching an empty account feature has no effect.
        assertEquals(loanAccountFeatures, accountData.getAccountFeatures());
    }

    @Test
    public void testTransactions() {
        AccountData accountData = new AccountData(mockAccount(DUMMY_ACCOUNT_ID_0));

        List<Transaction> transactionList0 = Arrays.asList(new Transaction(), new Transaction());
        List<Transaction> transactionList1 =
                Arrays.asList(new Transaction(), new Transaction(), new Transaction());

        accountData.updateTransactions(transactionList0);
        assertEquals(accountData.getTransactions().size(), 2);

        accountData.updateTransactions(transactionList1);
        // It should still only be one transaction cached. `cacheTransactions()` is not
        // accumulative.
        assertEquals(transactionList1, accountData.getTransactions());
    }

    @Test
    public void testGetTransactionsWhenDateLimitIsSpecified() {
        // given
        AccountData accountData = new AccountData(mockAccount(DUMMY_ACCOUNT_ID_0));

        Transaction transaction1 = new Transaction();
        transaction1.setDate(Date.from(Instant.parse("2021-07-12T23:59:59Z")));
        Transaction transaction2 = new Transaction();
        transaction2.setDate(Date.from(Instant.parse("2021-07-13T00:00:00Z")));
        Transaction transaction3 = new Transaction();
        transaction3.setDate(Date.from(Instant.parse("2021-07-13T00:00:00Z")));
        List<Transaction> transactions = Arrays.asList(transaction1, transaction2, transaction3);

        accountData.updateTransactions(transactions);
        accountData.setTransactionDateLimit(LocalDate.parse("2021-07-13"));

        // when and then
        assertEquals(Arrays.asList(transaction2, transaction3), accountData.getTransactions());
    }

    @Test
    public void testTransferDestinationPatterns() {
        AccountData accountData = new AccountData(mockAccount(DUMMY_ACCOUNT_ID_0));

        List<TransferDestinationPattern> transferDestinationPatterns0 =
                Collections.singletonList(new TransferDestinationPattern());

        List<TransferDestinationPattern> transferDestinationPatterns1 =
                Collections.singletonList(new TransferDestinationPattern());

        accountData.updateTransferDestinationPatterns(transferDestinationPatterns0);
        assertEquals(accountData.getTransferDestinationPatterns().size(), 1);

        accountData.updateTransferDestinationPatterns(transferDestinationPatterns1);

        // TransferDestinationPatterns are accumulative.
        assertEquals(accountData.getTransferDestinationPatterns().size(), 2);
    }

    private Account mockAccount(String uniqueId) {
        Account account = Mockito.mock(Account.class);
        Mockito.when(account.getBankId()).thenReturn(uniqueId);
        return account;
    }
}
