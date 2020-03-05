package se.tink.backend.aggregation.nxgen.agents.demo;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public class ItDemoAccountGeneratorTest {
    String testUserName = "Tink";
    String testProvider = "it-test-open-banking-redirect";

    @Test
    public void TestGenerateDeterministicSavingsAccountAccounts() {
        DemoSavingsAccount savingsAccount =
                DemoAccountDefinitionGenerator.getDemoSavingsAccounts(testUserName, testProvider);
        System.out.println(savingsAccount.getAccountId());

        IbanIdentifier expectedRecipientAccount = new IbanIdentifier("IT52X0300203280728575573739");
        AccountIdentifier expectedIdentifier =
                AccountIdentifier.create(
                        AccountIdentifier.Type.IBAN, expectedRecipientAccount.getIban());
        Assert.assertEquals("Savings Account Tink", savingsAccount.getAccountName());
        Assert.assertTrue(savingsAccount.getIdentifiers().contains(expectedIdentifier));

        Assert.assertEquals(42577.92, savingsAccount.getAccountBalance(), 0.0001);
    }

    @Test
    public void TestGenerateDeterministicTransactionalAccountAccounts() {
        DemoTransactionAccount transactionAccount =
                DemoAccountDefinitionGenerator.getDemoTransactionalAccount(
                        testUserName, testProvider);
        System.out.println(transactionAccount.getAccountId());

        IbanIdentifier expectedRecipientAccount = new IbanIdentifier("IT53X0300203280882749129712");
        AccountIdentifier expectedIdentifier =
                AccountIdentifier.create(
                        AccountIdentifier.Type.IBAN, expectedRecipientAccount.getIban());
        Assert.assertEquals("Checking Account Tink", transactionAccount.getAccountName());
        Assert.assertTrue(transactionAccount.getIdentifiers().contains(expectedIdentifier));

        Assert.assertEquals(551.25, transactionAccount.getBalance(), 0.0001);
    }
}
