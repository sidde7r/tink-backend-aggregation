package se.tink.backend.system.cli.cleanup;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.tink.backend.common.dao.AccountDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DeleteAccountsAndTransactionsCommandTest {
    @Mock AccountDao accountDao;
    @Mock TransactionDao transactionDao;
    @InjectMocks DeleteAccountsAndTransactionsCommand command;

    @Test
    public void deleteAccountAlongsideTransactions() {
        List<String> accountIds = singletonList("accountId");
        boolean dryRun = false;
        boolean deleteAccounts = true;
        //noinspection ConstantConditions
        command.run(dryRun, deleteAccounts, accountIds, accountDao, transactionDao);

        verify(transactionDao).deleteByAccountIds(accountIds);
        verify(accountDao).deleteByIds(accountIds);
    }

    @Test
    public void deleteTransactionsOnly() {
        List<String> accountIds = singletonList("accountId");
        boolean dryRun = false;
        boolean deleteAccounts = false;
        //noinspection ConstantConditions
        command.run(dryRun, deleteAccounts, accountIds, accountDao, transactionDao);

        verify(transactionDao).deleteByAccountIds(accountIds);
        verify(accountDao, never()).deleteByIds(accountIds);
    }

    @Test
    public void deleteWithDryRun() {
        List<String> accountIds = singletonList("accountId");
        boolean dryRun = true;
        boolean deleteAccounts = true;
        // noinspection ConstantConditions
        command.run(dryRun, deleteAccounts, accountIds, accountDao, transactionDao);

        verify(transactionDao, never()).deleteByAccountIds(accountIds);
        verify(accountDao, never()).deleteByIds(accountIds);
    }

    @Test
    public void deleteMultipleAccountsAlongsideTransactions() {
        List<String> accountIds = asList("accountId1", "accountId2", "accountId3");
        boolean dryRun = false;
        boolean deleteAccounts = true;
        //noinspection ConstantConditions
        command.run(dryRun, deleteAccounts, accountIds, accountDao, transactionDao);

        verify(transactionDao).deleteByAccountIds(accountIds);
        verify(accountDao).deleteByIds(accountIds);
    }

}
