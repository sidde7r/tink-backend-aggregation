package se.tink.backend.aggregation.nxgen.agents.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;
import org.iban4j.IbanUtil;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public class FrDemoAccountGeneratorTest {
    String testUserName = "Tink";
    String otherTestUserName = "NotTink";
    String testProvider = "fr-test-open-banking-redirect";

    @Test
    public void TestGenerateDeterministicSavingsAccountAccounts() {
        DemoSavingsAccount savingsAccount =
                DemoAccountDefinitionGenerator.getDemoSavingsAccounts(testUserName, testProvider);

        IbanIdentifier expectedRecipientAccount = new IbanIdentifier("FR2230713071000013668465660");
        AccountIdentifier expectedIdentifier =
                AccountIdentifier.create(
                        AccountIdentifier.Type.IBAN, expectedRecipientAccount.getIban());

        assertThat(savingsAccount.getAccountName()).isEqualTo("Savings Account Tink");
        assertThat(savingsAccount.getIdentifiers()).contains(expectedIdentifier);
        assertThat(savingsAccount.getAccountBalance()).isEqualByComparingTo(66361.68);
        assertThatCode(() -> IbanUtil.validate(savingsAccount.getAccountId()))
                .doesNotThrowAnyException();
    }

    @Test
    public void TestGenerateDeterministicTransactionalAccountAccounts() {
        DemoTransactionAccount transactionAccount =
                DemoAccountDefinitionGenerator.getDemoTransactionalAccount(
                        testUserName, testProvider);

        IbanIdentifier expectedRecipientAccount = new IbanIdentifier("FR3645724572000013668465660");
        AccountIdentifier expectedIdentifier =
                AccountIdentifier.create(
                        AccountIdentifier.Type.IBAN, expectedRecipientAccount.getIban());
        assertThat(transactionAccount.getAccountName()).isEqualTo("Checking Account Tink");
        assertThat(transactionAccount.getIdentifiers()).contains(expectedIdentifier);
        assertThat(transactionAccount.getBalance()).isEqualByComparingTo(649.74);
        assertThatCode(() -> IbanUtil.validate(transactionAccount.getAccountId()))
                .doesNotThrowAnyException();
    }

    @Test
    public void testIban4jRandomizer() {
        DemoSavingsAccount savingsAccount =
                DemoAccountDefinitionGenerator.getDemoSavingsAccounts(testUserName, testProvider);
        List<AccountIdentifier> ids = savingsAccount.getIdentifiers();
        assertThat(ids).isNotEmpty();
        ids.addAll(
                DemoAccountDefinitionGenerator.getDemoSavingsAccounts(testUserName, testProvider)
                        .getIdentifiers());
        ids.addAll(
                DemoAccountDefinitionGenerator.getDemoSavingsAccounts(testUserName, testProvider)
                        .getIdentifiers());
        long numberOfDistinctIdentifiers =
                ids.stream().map(AccountIdentifier::getIdentifier).distinct().count();
        assertThat(numberOfDistinctIdentifiers).isEqualTo(1L);
        ids.addAll(
                DemoAccountDefinitionGenerator.getDemoSavingsAccounts(
                                otherTestUserName, testProvider)
                        .getIdentifiers());
        ids.addAll(
                DemoAccountDefinitionGenerator.getDemoSavingsAccounts(
                                otherTestUserName, testProvider)
                        .getIdentifiers());
        numberOfDistinctIdentifiers =
                ids.stream().map(AccountIdentifier::getIdentifier).distinct().count();
        assertThat(numberOfDistinctIdentifiers).isEqualTo(2L);
    }
}
