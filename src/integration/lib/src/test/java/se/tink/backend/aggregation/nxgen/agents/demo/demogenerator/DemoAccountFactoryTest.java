package se.tink.backend.aggregation.nxgen.agents.demo.demogenerator;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoAccountDefinitionGenerator;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.i18n.Catalog;

public class DemoAccountFactoryTest {
    private static final Catalog catalog = new Catalog(Locale.US);
    private static final DemoTransactionAccount demoTransactionAccount =
            DemoAccountDefinitionGenerator.getDemoTransactionalAccount(
                    "tink", "uk-starling-oauth2");
    private static final DemoTransactionAccount demoTransactionAccountWithoutIdentifiers =
            DemoAccountDefinitionGenerator.getDemoTransactionalAccountWithoutIdentifiers(
                    "tink", "uk-starling-oauth2", 0);
    private static final DemoSavingsAccount demoSavingsAccount =
            DemoAccountDefinitionGenerator.getDemoSavingsAccounts("tink", "uk-starling-oauth2");

    @Test
    public void testFetchTransactionalAccounts() {
        ArrayList<TransactionalAccount> transactionalAccounts =
                Lists.newArrayList(
                        DemoAccountFactory.fetchTransactionalAccounts(
                                "GBP",
                                catalog,
                                Lists.newArrayList(demoTransactionAccount),
                                demoSavingsAccount));

        Assert.assertEquals(2, transactionalAccounts.size());

        TransactionalAccount checkingAccount = transactionalAccounts.get(0);
        TransactionalAccount savingsAccount = transactionalAccounts.get(1);

        // Checking Account
        Assert.assertEquals(AccountTypes.CHECKING, checkingAccount.getType());
        Assert.assertEquals("GBP", checkingAccount.getExactBalance().getCurrencyCode());
        Assert.assertEquals(69.16, checkingAccount.getExactBalance().getDoubleValue(), 0.001);
        Assert.assertEquals("21835381396957", checkingAccount.getAccountNumber());
        Assert.assertEquals("Checking Account tink", checkingAccount.getName());

        Assert.assertEquals("21835381396957", checkingAccount.getIdModule().getUniqueId());

        Assert.assertEquals(
                Lists.newArrayList(
                        AccountIdentifier.create(
                                AccountIdentifierType.SORT_CODE, "21835381396957", "testAccount")),
                checkingAccount.getIdentifiers());

        Assert.assertNotNull(checkingAccount.getExactAvailableBalance());
        Assert.assertEquals("GBP", checkingAccount.getExactAvailableBalance().getCurrencyCode());
        Assert.assertEquals(
                62.24, checkingAccount.getExactAvailableBalance().getDoubleValue(), 0.001);
        Assert.assertNotNull(checkingAccount.getExactCreditLimit());
        Assert.assertEquals("GBP", checkingAccount.getExactCreditLimit().getCurrencyCode());
        Assert.assertEquals(424.81, checkingAccount.getExactCreditLimit().getDoubleValue(), 0.001);

        // Savings Account
        Assert.assertEquals(AccountTypes.SAVINGS, savingsAccount.getType());
        Assert.assertEquals("GBP", savingsAccount.getExactBalance().getCurrencyCode());
        Assert.assertEquals(4146.07, savingsAccount.getExactBalance().getDoubleValue(), 0.001);
        Assert.assertEquals("21835361152705", savingsAccount.getAccountNumber());
        Assert.assertEquals("Savings Account tink", savingsAccount.getName());

        Assert.assertEquals("21835361152705", savingsAccount.getIdModule().getUniqueId());

        Assert.assertEquals(
                Lists.newArrayList(
                        AccountIdentifier.create(
                                AccountIdentifierType.SORT_CODE, "21835361152705", "testAccount")),
                savingsAccount.getIdentifiers());

        Assert.assertNull(savingsAccount.getExactAvailableBalance());
        Assert.assertNull(savingsAccount.getExactCreditLimit());
    }

    @Test
    public void testFetchTransactionalAccountsWithoutIdentifiers() {
        ArrayList<TransactionalAccount> transactionalAccounts =
                Lists.newArrayList(
                        DemoAccountFactory.fetchTransactionalAccounts(
                                "GBP",
                                catalog,
                                Lists.newArrayList(demoTransactionAccountWithoutIdentifiers),
                                demoSavingsAccount));

        Assert.assertEquals(2, transactionalAccounts.size());
    }
}
