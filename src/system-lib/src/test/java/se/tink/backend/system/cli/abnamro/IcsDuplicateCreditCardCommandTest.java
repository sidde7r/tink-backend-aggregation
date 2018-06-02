package se.tink.backend.system.cli.abnamro;

import com.google.common.collect.ImmutableListMultimap;
import java.util.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.core.Account;
import se.tink.backend.core.Transaction;
import se.tink.libraries.uuid.UUIDUtils;

public class IcsDuplicateCreditCardCommandTest {
    private Account account1;
    private Account account2;
    private static final String ACCOUNT_ID = "01234567891";
    private static final double BALANCE_ALPHA = 12.50;
    private static final double BALANCE_BETA = -15;
    private ImmutableListMultimap<String, Transaction> transactionByAccountId;
    private ImmutableListMultimap<String, Transaction> transactionByAccountIdWithoutOverlap;

    private Date dateLater = new Date(2017, 06, 06);
    private Date dateEarlier = new Date(2017, 05, 17);

    @Before
    public void setUp() {
        account1 = new Account();
        account2 = new Account();

        account1.setId(UUIDUtils.generateUUID());
        account2.setId(UUIDUtils.generateUUID());
        account1.setBankId("0123456789123456");
        account2.setBankId("0123456789165432");
        account1.setBalance(BALANCE_ALPHA);
        account2.setBalance(BALANCE_BETA);

        account1.setCertainDate(dateLater);
        account2.setCertainDate(dateEarlier);

        Transaction transaction1 = new Transaction();
        transaction1.setOriginalDate(dateLater);
        transaction1.setOriginalAmount(37.50);
        transaction1.setOriginalDescription("Sushi Bar");
        transaction1.setAccountId(account1.getId());

        Transaction transaction2 = new Transaction();
        transaction2.setOriginalDate(dateEarlier);
        transaction2.setOriginalAmount(BALANCE_BETA);
        transaction2.setOriginalDescription("Bookstore");
        transaction2.setAccountId(account1.getId());
        Transaction transaction3 = transaction2.clone();
        transaction3.setAccountId(account2.getId());

        transactionByAccountId = ImmutableListMultimap.of(account1.getId(), transaction1, account1.getId(),
                transaction2, account2.getId(), transaction3, account2.getId(), transaction3);
        transactionByAccountIdWithoutOverlap = ImmutableListMultimap.of(account1.getId(), transaction1,
                account2.getId(), transaction3);
    }

    @Test
    public void testCanMergeAccounts_twoAccountsWithNoHistory() {
        IcsDuplicateCreditCardCommand.AccountWithTransactions newAccount = new IcsDuplicateCreditCardCommand()
                .mergeAccount(ACCOUNT_ID, account1, account2, ImmutableListMultimap.of());

        Assert.assertEquals("Check number of transactions",0, newAccount.getTransactions().size());
        Assert.assertEquals("Check balance on account", BALANCE_ALPHA, newAccount.getAccount().getBalance(),
                0.1);
    }

    @Test
    public void testCanMergeAccounts_twoAccountsWithCommonHistory() {
        IcsDuplicateCreditCardCommand.AccountWithTransactions newAccount = new IcsDuplicateCreditCardCommand()
                .mergeAccount(ACCOUNT_ID, account1, account2, transactionByAccountId);

        Assert.assertEquals("Check number of transactions",3, newAccount.getTransactions().size());
        Assert.assertEquals("Check balance on account", BALANCE_ALPHA, newAccount.getAccount().getBalance(),
                0.1);
        Assert.assertEquals("Certain date is inherited from the newer account", dateLater,
                newAccount.getAccount().getCertainDate());
    }

    @Test
    public void testCanMergeAccounts_twoAccountsWithCommonHistoryReverse() {
        IcsDuplicateCreditCardCommand.AccountWithTransactions newAccount = new IcsDuplicateCreditCardCommand()
                .mergeAccount(ACCOUNT_ID, account2, account1, transactionByAccountId);

        Assert.assertEquals("Check number of transactions",3, newAccount.getTransactions().size());
        Assert.assertEquals("Check balance on account", BALANCE_ALPHA, newAccount.getAccount().getBalance(),
                0.1);
        Assert.assertEquals("Certain date is inherited from the newer account", dateLater,
                newAccount.getAccount().getCertainDate());
    }

    @Test
    public void testCanMergeAccounts_twoAccountsWithoutOverlap() {
        IcsDuplicateCreditCardCommand.AccountWithTransactions newAccount = new IcsDuplicateCreditCardCommand()
                .mergeAccount(ACCOUNT_ID, account1, account2, transactionByAccountIdWithoutOverlap);

        Assert.assertEquals("Check number of transactions",2, newAccount.getTransactions().size());
        Assert.assertEquals("Check balance on account", BALANCE_ALPHA, newAccount.getAccount().getBalance(),
                0.1);
        Assert.assertEquals("Certain date is inherited from the newer account", dateLater,
                newAccount.getAccount().getCertainDate());
    }

    @Test
    public void testCanMergeAccounts_withoutCertainDate() {
        account1.setCertainDate(null);
        account2.setCertainDate(null);
        IcsDuplicateCreditCardCommand.AccountWithTransactions newAccount = new IcsDuplicateCreditCardCommand()
                .mergeAccount(ACCOUNT_ID, account1, account2, transactionByAccountId);

        Assert.assertEquals("Certain date for new account is null", null, newAccount.getAccount().getCertainDate());
    }

    @Test
    public void testCanMergeAccounts_withoutSingleCertainDate() {
        account2.setCertainDate(null);
        IcsDuplicateCreditCardCommand.AccountWithTransactions newAccount = new IcsDuplicateCreditCardCommand()
                .mergeAccount(ACCOUNT_ID, account1, account2, transactionByAccountId);

        Assert.assertEquals("Certain date for new account is inherited from the account where it isn't null",
                account1.getCertainDate(), newAccount.getAccount().getCertainDate());
    }
}
