package se.tink.libraries.abnamro.utils.creditcards;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.Test;
import se.tink.backend.core.Account;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.abnamro.utils.creditcards.DuplicateAccountMatcher;
import static org.assertj.core.api.Assertions.assertThat;

public class DuplicateAccountMatcherTest {
    @Test
    public void testEmptyInputs() {

        DateTime day1 = DateTime.parse("2017-10-01");
        DateTime day2 = DateTime.parse("2017-10-02");
        DateTime day3 = DateTime.parse("2017-10-03");

        String userId = StringUtils.generateUUID();

        Account account1 = createAccount(userId, "account-old");
        Account account2 = createAccount(userId, "account-new");

        List<Transaction> account1Transactions = ImmutableList.of(
                createTransaction(account1, "trx", 1.0, day1),
                createTransaction(account1, "trx", 1.0, day2));

        List<Transaction> account2Transactions = ImmutableList.of(
                createTransaction(account2, "trx", 1.0, day2),
                createTransaction(account2, "trx", 1.0, day3));

        DuplicateAccountMatcher matcher = DuplicateAccountMatcher.builder()
                .withMutualTransactionThreshold(1) // Require 1 mutual transaction
                .withAccount1(account1)
                .withAccount2(account2)
                .withAccount1CreatedAt(DateTime.now().minusDays(10).toDate())
                .withAccount2CreatedAt(DateTime.now().toDate())
                .withAccount1Transactions(account1Transactions)
                .withAccount2Transactions(account2Transactions)
                .build();

        assertThat(matcher.isDuplicate()).isTrue();
        assertThat(matcher.getOldAccount().getBankId()).isEqualTo("account-old");
        assertThat(matcher.getOldAccount().getAllTransactions()).hasSize(2);
        assertThat(matcher.getOldAccount().getUniqueTransactions()).hasSize(1);

        assertThat(matcher.getNewAccount().getBankId()).isEqualTo("account-new");
        assertThat(matcher.getNewAccount().getAllTransactions()).hasSize(2);
        assertThat(matcher.getNewAccount().getUniqueTransactions()).hasSize(1);

        assertThat(matcher.getMutualTransactions()).hasSize(1);
    }

    private Transaction createTransaction(Account account, String description, Double amount, DateTime date) {
        Transaction transaction = new Transaction();
        transaction.setId(StringUtils.generateUUID());
        transaction.setOriginalDescription(description);
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setOriginalAmount(amount);
        transaction.setDate(date.toDate());
        transaction.setOriginalDate(date.toDate());
        transaction.setAccountId(account.getId());
        transaction.setUserId(account.getUserId());

        return transaction;
    }

    public Account createAccount(String userId, String name) {
        Account account = new Account();
        account.setName(name);
        account.setBankId(name);
        account.setBalance(100);
        account.setUserId(userId);
        return account;
    }
}
