package se.tink.backend.system.controllers;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.core.Account;
import se.tink.libraries.metrics.MetricRegistry;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static se.tink.backend.core.AccountTypes.LOAN;

public class AccountControllerTest {

    private AccountController accountController;

    @Before
    public void setUp() throws Exception {
        accountController = new AccountController(null, null, null, null, null, null, null, new MetricRegistry());

    }

    @Test
    public void saveAndMarkNotClosedIfImported() {
        Account account = new Account();
        account.setId("accountId");
        account.setClosed(true);

        assertEquals(
                singletonList(account),
                accountController.prepareToSave(singletonList(account), singletonList("accountId"), ""));
        assertFalse(account.isClosed());
    }

    @Test
    public void saveAndMarkClosedIfNotImported() {
        Account account = new Account();
        account.setId("accountId");
        account.setClosed(false);

        assertEquals(
                singletonList(account),
                accountController.prepareToSave(singletonList(account), emptyList(), ""));
        assertTrue(account.isClosed());
    }

    @Test
    public void saveAndSetBalanceToZeroIfNotImported() {
        Account account = new Account();
        account.setId("accountId");
        account.setClosed(false);
        account.setBalance(20.0);

        assertEquals(
                singletonList(account),
                accountController.prepareToSave(singletonList(account), emptyList(), ""));
        assertEquals(account.getBalance(), 0.0, 0.001);
    }

    @Test
    public void saveAndExcludeIfNotImportedClosedNotUserExcludedLoan() {
        Account account = new Account();
        account.setId("accountId");
        account.setExcluded(false);
        account.setClosed(true);
        account.setUserModifiedExcluded(false);
        account.setType(LOAN);

        assertEquals(
                singletonList(account),
                accountController.prepareToSave(singletonList(account), emptyList(), ""));
        assertTrue(account.isExcluded());
    }

    @Test
    public void keepImportedAccountExcludedIfUserExcluded() {
        Account account = new Account();
        account.setId("accountId");
        account.setClosed(false);
        account.setUserModifiedExcluded(true);
        account.setExcluded(true);

        assertTrue(accountController
                .prepareToSave(singletonList(account), singletonList("accountId"), "")
                .isEmpty());
    }

    @Test
    public void skipImportedNotExcludedNotClosedNotUserModified() {
        Account account = new Account();
        account.setId("accountId");
        account.setExcluded(false);
        account.setClosed(false);
        account.setUserModifiedExcluded(false);

        assertTrue(accountController
                .prepareToSave(singletonList(account), singletonList("accountId"), "")
                .isEmpty());
    }

    @Test
    public void saveAndActivateImportedNotExcludedByUser() {
        Account account = new Account();
        account.setId("accountId");
        account.setExcluded(true);
        account.setClosed(false);
        account.setUserModifiedExcluded(false);

        assertEquals(
                singletonList(account),
                accountController.prepareToSave(singletonList(account), singletonList("accountId"), ""));
    }

    @Test
    public void doNotSaveIfNotImportedNotUserExcludedNotLoan() {
        Account account = new Account();
        account.setId("requestId");
        account.setClosed(true);

        assertTrue(accountController
                .prepareToSave(singletonList(account), emptyList(), "")
                .isEmpty());

        assertFalse(account.isExcluded());
    }

    @Test
    public void doNotExcludeAccountWhenUserModifiedExcluded() {
        Account account = new Account();
        account.setUserModifiedExcluded(true);
        account.setExcluded(false);
        account.setClosed(true);

        assertTrue(accountController
                .prepareToSave(singletonList(account), emptyList(), "")
                .isEmpty());
        assertFalse(account.isExcluded());
    }

    @Test
    public void closeNotImportedUserModifiedExcludedAccount() {
        Account account = new Account();
        account.setId("requestId");
        account.setUserModifiedExcluded(true);
        account.setClosed(false);

        assertEquals(
                singletonList(account),
                accountController.prepareToSave(singletonList(account), emptyList(), ""));
        assertTrue(account.isClosed());
    }

}
