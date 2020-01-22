package se.tink.backend.aggregation.nxgen.agents.demo;

import java.net.URI;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.UkIdentifier;

public class UkDemoAccountGeneratorTest {

    String testUserName = "Tink";
    String testProvider = "uk-test-open-banking-redirect";

    @Test
    public void TestGenerateDeterministicTransactionalAccountAccounts() {
        DemoSavingsAccount savingsAccount =
                DemoAccountDefinitionGenerator.getDemoSavingsAccounts(testUserName, testProvider);

        UkIdentifier expectedRecipientAccount = new UkIdentifier("44-44-44-46744674");
        AccountIdentifier expectedIdentifier =
                AccountIdentifier.create(URI.create(expectedRecipientAccount.toUriAsString()));

        Assert.assertTrue(savingsAccount.getAccountName().equals("Savings Account Tink"));
        Assert.assertTrue(savingsAccount.getIdentifiers().contains(expectedIdentifier));

        System.out.println(savingsAccount.getAccountId());
        System.out.println(savingsAccount.getAccountBalance());
    }
}
