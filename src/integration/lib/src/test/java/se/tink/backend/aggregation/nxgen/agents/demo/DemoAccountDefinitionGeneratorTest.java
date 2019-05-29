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
}
