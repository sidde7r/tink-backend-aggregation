package se.tink.backend.aggregation.nxgen.agents.demo;

import java.net.URI;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;

public class UkDemoAccountGeneratorTest {

    String testUserName = "Tink";
    String testProvider = "uk-test-open-banking-redirect";

    @Test
    public void TestGenerateDeterministicSavingsAccountAccounts() {
        DemoSavingsAccount savingsAccount =
                DemoAccountDefinitionGenerator.getDemoSavingsAccounts(testUserName, testProvider);
        System.out.println(savingsAccount.getAccountId());

        SortCodeIdentifier expectedRecipientAccount = new SortCodeIdentifier("21835371417779");
        AccountIdentifier expectedIdentifier =
                AccountIdentifier.create(URI.create(expectedRecipientAccount.toUriAsString()));
        Assert.assertEquals("Savings Account Tink", savingsAccount.getAccountName());
        Assert.assertTrue(savingsAccount.getIdentifiers().contains(expectedIdentifier));

        Assert.assertEquals(51991.94, savingsAccount.getAccountBalance(), 0.0001);
    }

    @Test
    public void TestGenerateDeterministicTransactionalAccountAccounts() {
        DemoTransactionAccount transactionAccount =
                DemoAccountDefinitionGenerator.getDemoTransactionalAccount(
                        testUserName, testProvider);
        System.out.println(transactionAccount.getAccountId());

        SortCodeIdentifier expectedRecipientAccount = new SortCodeIdentifier("21835371527841");
        AccountIdentifier expectedIdentifier =
                AccountIdentifier.create(
                        AccountIdentifierType.SORT_CODE, expectedRecipientAccount.toString());
        Assert.assertEquals("Checking Account Tink", transactionAccount.getAccountName());
        Assert.assertTrue(transactionAccount.getIdentifiers().contains(expectedIdentifier));

        Assert.assertEquals(715.27, transactionAccount.getBalance(), 0.0001);
    }
}
