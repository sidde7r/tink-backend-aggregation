package se.tink.backend.aggregation.nxgen.agents.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public class ItDemoAccountGeneratorTest {
    String testUserName = "Tink";
    String otherTestUserName = "NotTink";
    String testProvider = "it-test-open-banking-redirect";

    @Test
    public void TestGenerateDeterministicSavingsAccountAccounts() {
        DemoSavingsAccount savingsAccount =
                DemoAccountDefinitionGenerator.getDemoSavingsAccounts(testUserName, testProvider);
        System.out.println(savingsAccount.getAccountId());

        IbanIdentifier expectedRecipientAccount = new IbanIdentifier("IT12X0542811101000700088930");
        AccountIdentifier expectedIdentifier =
                AccountIdentifier.create(
                        AccountIdentifierType.IBAN, expectedRecipientAccount.getIban());
        Assert.assertEquals("Savings Account Tink", savingsAccount.getAccountName());
        assertThat(savingsAccount.getIdentifiers()).contains(expectedIdentifier);

        Assert.assertEquals(42577.92, savingsAccount.getAccountBalance(), 0.0001);
    }

    @Test
    public void TestGenerateDeterministicTransactionalAccountAccounts() {
        DemoTransactionAccount transactionAccount =
                DemoAccountDefinitionGenerator.getDemoTransactionalAccount(
                        testUserName, testProvider);
        System.out.println(transactionAccount.getAccountId());

        IbanIdentifier expectedRecipientAccount = new IbanIdentifier("IT04X0542811101001285970723");
        AccountIdentifier expectedIdentifier =
                AccountIdentifier.create(
                        AccountIdentifierType.IBAN, expectedRecipientAccount.getIban());
        Assert.assertEquals("Checking Account Tink", transactionAccount.getAccountName());
        assertThat(transactionAccount.getIdentifiers()).contains(expectedIdentifier);
        Assert.assertEquals(686.0, transactionAccount.getBalance(), 0.0001);
    }

    @Test
    public void testIban4jRandomizer() {
        DemoSavingsAccount savingsAccount =
                DemoAccountDefinitionGenerator.getDemoSavingsAccounts(testUserName, testProvider);
        List<AccountIdentifier> ids = savingsAccount.getIdentifiers();
        Assert.assertFalse(ids.isEmpty());
        ids.addAll(
                DemoAccountDefinitionGenerator.getDemoSavingsAccounts(testUserName, testProvider)
                        .getIdentifiers());
        ids.addAll(
                DemoAccountDefinitionGenerator.getDemoSavingsAccounts(testUserName, testProvider)
                        .getIdentifiers());
        Assert.assertEquals(
                1, ids.stream().map(AccountIdentifier::getIdentifier).distinct().count());
        ids.addAll(
                DemoAccountDefinitionGenerator.getDemoSavingsAccounts(
                                otherTestUserName, testProvider)
                        .getIdentifiers());
        ids.addAll(
                DemoAccountDefinitionGenerator.getDemoSavingsAccounts(
                                otherTestUserName, testProvider)
                        .getIdentifiers());
        Assert.assertEquals(
                2, ids.stream().map(AccountIdentifier::getIdentifier).distinct().count());
    }
}
