package se.tink.backend.common.search;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Test;
import se.tink.backend.core.Account;
import se.tink.backend.core.Transaction;
import static org.assertj.core.api.Assertions.assertThat;

public class SuggestTransactionsSearcherTest {

    @Test
    public void testFilterWithEmptyLists() {

        List<Account> accounts = Lists.newArrayList();
        List<Transaction> transactions = Lists.newArrayList();

        assertThat(SuggestTransactionsSearcher.filterTransactionsOnIncludedAccounts(accounts, transactions)).isEmpty();
    }

    @Test
    public void testFilterWithEmptyAccountList() {

        List<Account> accounts = Lists.newArrayList();
        List<Transaction> transactions = Lists.newArrayList(new Transaction());

        assertThat(SuggestTransactionsSearcher.filterTransactionsOnIncludedAccounts(accounts, transactions)).isEmpty();
    }

    @Test
    public void testFilterWithIncludedAccount() {

        Account account = new Account();

        List<Account> accounts = Lists.newArrayList(account);

        Transaction transaction = new Transaction();
        transaction.setAccountId(account.getId());

        List<Transaction> transactions = Lists.newArrayList(transaction);

        assertThat(SuggestTransactionsSearcher.filterTransactionsOnIncludedAccounts(accounts, transactions)).hasSize(1);
    }

    @Test
    public void testFilterWithIncludedAndExcludedAccount() {

        Account includedAccount = new Account();
        Account excludedAccount = new Account();
        excludedAccount.setExcluded(true);

        List<Account> accounts = Lists.newArrayList(includedAccount, excludedAccount);

        Transaction includedTransaction = new Transaction();
        includedTransaction.setAccountId(includedAccount.getId());

        Transaction excludedTransaction = new Transaction();
        excludedTransaction.setAccountId(excludedAccount.getId());

        List<Transaction> transactions = Lists.newArrayList(includedTransaction, excludedTransaction);

        assertThat(SuggestTransactionsSearcher.filterTransactionsOnIncludedAccounts(accounts, transactions))
                .containsOnly(includedTransaction);
    }
}
