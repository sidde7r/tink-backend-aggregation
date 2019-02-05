package se.tink.backend.aggregation.nxgen.agents.demo;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;

public class DemoAccountDefinitionGeneratorTest {

    String testUserName = "Tink";
    String testProvider = "BankIdTest";

    @Test
    public void TestGenerateDeterministicTransactionalAccount() {
        DemoTransactionAccount transactionalAccountAccounts = DemoAccountDefinitionGenerator
                .getDemoTransactionalAccount(testUserName, testProvider);

        Assert.assertTrue(transactionalAccountAccounts.getBalance() == 618.75);
        Assert.assertTrue(transactionalAccountAccounts.getAccountId().equals("4950-618754677750"));
        Assert.assertTrue(transactionalAccountAccounts.getAccountName().equals("Checking Account Tink"));
    }


    @Test
    public void TestGenerateDeterministicTransactionalAccountAccounts() {
        DemoSavingsAccount savingsAccount = DemoAccountDefinitionGenerator
                .getDemoSavingsAccounts(testUserName, testProvider);

        Assert.assertTrue(savingsAccount.getAccountBalance() ==  42446.25);
        Assert.assertTrue(savingsAccount.getAccountId().equals("4410-551254244625"));
        Assert.assertTrue(savingsAccount.getAccountName().equals("Savings Account Tink"));
    }

}
