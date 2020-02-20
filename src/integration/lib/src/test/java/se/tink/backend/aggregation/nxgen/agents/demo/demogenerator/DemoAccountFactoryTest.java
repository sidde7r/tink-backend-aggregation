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
import se.tink.libraries.i18n.Catalog;

public class DemoAccountFactoryTest {
    private static final Catalog catalog = new Catalog(Locale.ENGLISH);
    private static final DemoTransactionAccount demoTransactionAccount =
            DemoAccountDefinitionGenerator.getDemoTransactionalAccount(
                    "tink", "uk-starling-oauth2");
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
        Assert.assertEquals(813.96, checkingAccount.getExactBalance().getDoubleValue(), 0.001);
        Assert.assertEquals("23147081396957", checkingAccount.getAccountNumber());
        Assert.assertEquals("Checking Account tink", checkingAccount.getName());

        Assert.assertEquals("23147081396957", checkingAccount.getIdModule().getUniqueId());

        Assert.assertEquals(
                Lists.newArrayList(
                        AccountIdentifier.create(
                                AccountIdentifier.Type.SORT_CODE, "23147081396957", "testAccount")),
                checkingAccount.getIdentifiers());

        Assert.assertNotNull(checkingAccount.getExactAvailableBalance());
        Assert.assertEquals("GBP", checkingAccount.getExactAvailableBalance().getCurrencyCode());
        Assert.assertEquals(
                732.56, checkingAccount.getExactAvailableBalance().getDoubleValue(), 0.001);
        Assert.assertNotNull(checkingAccount.getExactCreditLimit());
        Assert.assertEquals("GBP", checkingAccount.getExactCreditLimit().getCurrencyCode());
        Assert.assertEquals(5000.0, checkingAccount.getExactCreditLimit().getDoubleValue(), 0.001);

        // Savings Account
        Assert.assertEquals(AccountTypes.SAVINGS, savingsAccount.getType());
        Assert.assertEquals("GBP", savingsAccount.getExactBalance().getCurrencyCode());
        Assert.assertEquals(48799.29, savingsAccount.getExactBalance().getDoubleValue(), 0.001);
        Assert.assertEquals("23147061152705", savingsAccount.getAccountNumber());
        Assert.assertEquals("Savings Account tink", savingsAccount.getName());

        Assert.assertEquals("23147061152705", savingsAccount.getIdModule().getUniqueId());

        Assert.assertEquals(
                Lists.newArrayList(
                        AccountIdentifier.create(
                                AccountIdentifier.Type.SORT_CODE, "23147061152705", "testAccount")),
                savingsAccount.getIdentifiers());

        Assert.assertNull(savingsAccount.getExactAvailableBalance());
        Assert.assertNull(savingsAccount.getExactCreditLimit());
    }
}
