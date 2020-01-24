package se.tink.backend.aggregation.nxgen.agents.demo;

import java.net.URI;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

public class DemoAccountDefinitionGeneratorTest {

    String testUserName = "Tink";
    String testProvider = "BankIdTest";

    @Test
    public void TestGenerateDeterministicTransactionalAccount() {
        DemoTransactionAccount transactionalAccountAccounts =
                DemoAccountDefinitionGenerator.getDemoTransactionalAccount(
                        testUserName, testProvider);

        SwedishIdentifier expectedRecipientAccount = new SwedishIdentifier("4950-618754677750");
        AccountIdentifier expectedIdentifier =
                AccountIdentifier.create(URI.create(expectedRecipientAccount.toUriAsString()));

        Assert.assertTrue(transactionalAccountAccounts.getBalance() == 618.75);
        Assert.assertTrue(transactionalAccountAccounts.getAccountId().equals("4950-618754677750"));
        Assert.assertTrue(
                transactionalAccountAccounts.getAccountName().equals("Checking Account Tink"));
        Assert.assertTrue(
                transactionalAccountAccounts.getIdentifiers().contains(expectedIdentifier));
    }

    @Test
    public void TestGenerateDeterministicTransactionalAccountAccounts() {
        DemoSavingsAccount savingsAccount =
                DemoAccountDefinitionGenerator.getDemoSavingsAccounts(testUserName, testProvider);

        SwedishIdentifier expectedRecipientAccount = new SwedishIdentifier("4410-551254244625");
        AccountIdentifier expectedIdentifier =
                AccountIdentifier.create(URI.create(expectedRecipientAccount.toUriAsString()));

        Assert.assertTrue(savingsAccount.getAccountBalance() == 42446.25);
        Assert.assertTrue(savingsAccount.getAccountId().equals("4410-551254244625"));
        Assert.assertTrue(savingsAccount.getAccountName().equals("Savings Account Tink"));
        Assert.assertTrue(savingsAccount.getIdentifiers().contains(expectedIdentifier));
    }

    @Test
    public void testGenerateTransactionalAccountWithDifferentKeys() {
        DemoTransactionAccount transactionalAccount1 =
                DemoAccountDefinitionGenerator.getDemoTransactionalAccount(
                        testUserName, testProvider, 0);
        DemoTransactionAccount transactionalAccount2 =
                DemoAccountDefinitionGenerator.getDemoTransactionalAccount(
                        testUserName, testProvider, 1);

        AccountIdentifier expectedIdentifier1 = new SwedishIdentifier("4950-618754677750");
        AccountIdentifier expectedIdentifier2 = new SwedishIdentifier("1097-699724898040");

        Assert.assertNotEquals(
                transactionalAccount1.getAccountId(), transactionalAccount2.getAccountId());
        Assert.assertEquals(618.75, transactionalAccount1.getBalance(), 0.0001);
        Assert.assertEquals("4950-618754677750", transactionalAccount1.getAccountId());
        Assert.assertEquals("Checking Account Tink", transactionalAccount1.getAccountName());
        Assert.assertTrue(transactionalAccount1.getIdentifiers().contains(expectedIdentifier1));

        Assert.assertEquals(699.72, transactionalAccount2.getBalance(), 0.0001);
        Assert.assertEquals("1097-699724898040", transactionalAccount2.getAccountId());
        Assert.assertEquals("Checking Account Tink 1", transactionalAccount2.getAccountName());
        Assert.assertTrue(transactionalAccount2.getIdentifiers().contains(expectedIdentifier2));
    }

    @Test
    public void testGenerateSavingsAccountWithDifferentKeys() {
        DemoSavingsAccount savingsAccount1 =
                DemoAccountDefinitionGenerator.getDemoSavingsAccounts(
                        testUserName, testProvider, 0);
        DemoSavingsAccount savingsAccount2 =
                DemoAccountDefinitionGenerator.getDemoSavingsAccounts(
                        testUserName, testProvider, 1);

        SwedishIdentifier expectedIdentifier1 = new SwedishIdentifier("4410-551254244625");
        AccountIdentifier expectedIdentifier2 = new SwedishIdentifier("4957-619654944807");

        Assert.assertNotEquals(savingsAccount1.getAccountId(), savingsAccount2.getAccountId());
        Assert.assertEquals(42446.25, savingsAccount1.getAccountBalance(), 0.0001);
        Assert.assertEquals("4410-551254244625", savingsAccount1.getAccountId());
        Assert.assertEquals("Savings Account Tink", savingsAccount1.getAccountName());
        Assert.assertTrue(savingsAccount1.getIdentifiers().contains(expectedIdentifier1));

        Assert.assertEquals(49448.07, savingsAccount2.getAccountBalance(), 0.0001);
        Assert.assertEquals("4957-619654944807", savingsAccount2.getAccountId());
        Assert.assertEquals("Savings Account Tink 1", savingsAccount2.getAccountName());
        Assert.assertTrue(savingsAccount2.getIdentifiers().contains(expectedIdentifier2));
    }
}
