package se.tink.libraries.account_data_cache;

import java.util.Collections;
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

        Assert.assertEquals(loanAccountFeatures, accountData.getAccountFeatures());

        accountData.updateAccountFeatures(emptyAccountFeatures);

        // Still loan features, caching an empty account feature has no effect.
        Assert.assertEquals(loanAccountFeatures, accountData.getAccountFeatures());
    }

    @Test
    public void testTransactions() {
        AccountData accountData = new AccountData(mockAccount(DUMMY_ACCOUNT_ID_0));

        List<Transaction> transactionList0 = Collections.singletonList(new Transaction());
        List<Transaction> transactionList1 = Collections.singletonList(new Transaction());

        accountData.updateTransactions(transactionList0);
        Assert.assertEquals(accountData.getTransactions().size(), 1);

        accountData.updateTransactions(transactionList1);
        // It should still only be one transaction cached. `cacheTransactions()` is not
        // accumulative.
        Assert.assertEquals(accountData.getTransactions().size(), 1);

        Assert.assertSame(transactionList1, accountData.getTransactions());
        Assert.assertNotSame(transactionList0, accountData.getTransactions());
    }

    @Test
    public void testTransferDestinationPatterns() {
        AccountData accountData = new AccountData(mockAccount(DUMMY_ACCOUNT_ID_0));

        List<TransferDestinationPattern> transferDestinationPatterns0 =
                Collections.singletonList(new TransferDestinationPattern());

        List<TransferDestinationPattern> transferDestinationPatterns1 =
                Collections.singletonList(new TransferDestinationPattern());

        accountData.updateTransferDestinationPatterns(transferDestinationPatterns0);
        Assert.assertEquals(accountData.getTransferDestinationPatterns().size(), 1);

        accountData.updateTransferDestinationPatterns(transferDestinationPatterns1);

        // TransferDestinationPatterns are accumulative.
        Assert.assertEquals(accountData.getTransferDestinationPatterns().size(), 2);
    }

    private Account mockAccount(String uniqueId) {
        Account account = Mockito.mock(Account.class);
        Mockito.when(account.getBankId()).thenReturn(uniqueId);
        return account;
    }
}
