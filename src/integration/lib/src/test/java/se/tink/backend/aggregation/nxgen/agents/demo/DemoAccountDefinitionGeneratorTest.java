package se.tink.backend.aggregation.nxgen.agents.demo;

import com.google.common.collect.Lists;
import java.net.URI;
import org.iban4j.CountryCode;
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

        SwedishIdentifier expectedRecipientAccount = new SwedishIdentifier("1100-742505197500");
        AccountIdentifier expectedIdentifier =
                AccountIdentifier.create(URI.create(expectedRecipientAccount.toUriAsString()));

        Assert.assertEquals(742.5, transactionalAccountAccounts.getBalance(), 0.0);
        Assert.assertEquals("1100-742505197500", transactionalAccountAccounts.getAccountId());
        Assert.assertEquals("Checking Account Tink", transactionalAccountAccounts.getAccountName());
        Assert.assertTrue(
                transactionalAccountAccounts.getIdentifiers().contains(expectedIdentifier));

        Assert.assertTrue(transactionalAccountAccounts.getAvailableBalance().isPresent());
        Assert.assertEquals(
                668.25, transactionalAccountAccounts.getAvailableBalance().get(), 0.001);

        Assert.assertTrue(transactionalAccountAccounts.getCreditLimit().isPresent());
        Assert.assertEquals(5000.0, transactionalAccountAccounts.getCreditLimit().get(), 0.001);
    }

    @Test
    public void TestGenerateDeterministicTransactionalAccountZeroBalance() {
        DemoTransactionAccount transactionalAccountAccounts =
                DemoAccountDefinitionGenerator.getDemoTransactionalAccountWithZeroBalance(
                        testUserName, testProvider);

        SwedishIdentifier expectedRecipientAccount = new SwedishIdentifier("5472-684005458320");
        AccountIdentifier expectedIdentifier =
                AccountIdentifier.create(URI.create(expectedRecipientAccount.toUriAsString()));

        Assert.assertEquals(0.0, transactionalAccountAccounts.getBalance(), 0.00);
        Assert.assertEquals("5472-684005458320", transactionalAccountAccounts.getAccountId());
        Assert.assertEquals(
                "Checking Account Tink zero balance",
                transactionalAccountAccounts.getAccountName());
        Assert.assertEquals(
                Lists.newArrayList(expectedIdentifier),
                transactionalAccountAccounts.getIdentifiers());

        Assert.assertTrue(transactionalAccountAccounts.getAvailableBalance().isPresent());
        Assert.assertEquals(0.0, transactionalAccountAccounts.getAvailableBalance().get(), 0.00);

        Assert.assertFalse(transactionalAccountAccounts.getCreditLimit().isPresent());
    }

    @Test
    public void TestGenerateDeterministicSavingsAccount() {
        DemoSavingsAccount savingsAccount =
                DemoAccountDefinitionGenerator.getDemoSavingsAccounts(testUserName, testProvider);

        SwedishIdentifier expectedRecipientAccount = new SwedishIdentifier("5390-673754904900");
        AccountIdentifier expectedIdentifier =
                AccountIdentifier.create(URI.create(expectedRecipientAccount.toUriAsString()));

        Assert.assertEquals(49049.0, savingsAccount.getAccountBalance(), 0.0);
        Assert.assertEquals("5390-673754904900", savingsAccount.getAccountId());
        Assert.assertEquals("Savings Account Tink", savingsAccount.getAccountName());
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

        AccountIdentifier expectedIdentifier1 = new SwedishIdentifier("1100-742505197500");
        AccountIdentifier expectedIdentifier2 = new SwedishIdentifier("1097-699724898040");

        Assert.assertNotEquals(
                transactionalAccount1.getAccountId(), transactionalAccount2.getAccountId());
        Assert.assertEquals(742.5, transactionalAccount1.getBalance(), 0.0001);
        Assert.assertEquals("1100-742505197500", transactionalAccount1.getAccountId());
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

        SwedishIdentifier expectedIdentifier1 = new SwedishIdentifier("5390-673754904900");
        AccountIdentifier expectedIdentifier2 = new SwedishIdentifier("1101-784896153537");

        Assert.assertNotEquals(savingsAccount1.getAccountId(), savingsAccount2.getAccountId());
        Assert.assertEquals(49049.0, savingsAccount1.getAccountBalance(), 0.0001);
        Assert.assertEquals("5390-673754904900", savingsAccount1.getAccountId());
        Assert.assertEquals("Savings Account Tink", savingsAccount1.getAccountName());
        Assert.assertTrue(savingsAccount1.getIdentifiers().contains(expectedIdentifier1));

        Assert.assertEquals(61535.37, savingsAccount2.getAccountBalance(), 0.0001);
        Assert.assertEquals("1101-784896153537", savingsAccount2.getAccountId());
        Assert.assertEquals("Savings Account Tink 1", savingsAccount2.getAccountName());
        Assert.assertTrue(savingsAccount2.getIdentifiers().contains(expectedIdentifier2));
    }

    @Test
    public void shouldGenerateCorrectIbanForItaly() {
        // given
        String deterministicKey = "0123456789";
        String accountNumber = "00" + deterministicKey;
        String bankCode = "05428";
        String branchCode = "11101";
        String checkChars = "X";
        String ibanPattern =
                CountryCode.IT + "\\d{2}" + checkChars + bankCode + branchCode + accountNumber;

        // when
        String iban = DemoAccountDefinitionGenerator.generateAccountNumbersIT(deterministicKey);

        // then
        Assert.assertTrue(iban.matches(ibanPattern));
    }

    @Test
    public void shouldGenerateCorrectIbanForFrance() {
        // given
        String deterministicKey = "0123456789";
        String accountNumber = "00" + deterministicKey;
        String bankCode = "20041";
        String branchCode = "01005";
        String checkChars = "0";
        String ibanPattern =
                CountryCode.FR + "\\d{2}" + bankCode + branchCode + accountNumber + checkChars;

        // when
        String iban = DemoAccountDefinitionGenerator.generateAccountNumbersFR(deterministicKey);

        // then
        Assert.assertTrue(iban.matches(ibanPattern));
    }

    @Test
    public void shouldGenerateCorrectIbanForGermany() {
        // given
        String deterministicKey = "0123456789";
        String bankCode = "37040044";
        String ibanPattern = CountryCode.DE + "\\d{2}" + bankCode + deterministicKey;

        // when
        String iban = DemoAccountDefinitionGenerator.generateAccountNumbersDE(deterministicKey);

        // then
        Assert.assertTrue(iban.matches(ibanPattern));
    }

    @Test
    public void shouldGenerateCorrectIbanForGermanyIfLongerKey() {
        // given
        String deterministicKey = "0123456789111";
        String accountNumber = "0123456789";
        String bankCode = "37040044";
        String ibanPattern = CountryCode.DE + "\\d{2}" + bankCode + accountNumber;

        // when
        String iban = DemoAccountDefinitionGenerator.generateAccountNumbersDE(deterministicKey);

        // then
        Assert.assertTrue(iban.matches(ibanPattern));
    }

    @Test
    public void shouldGenerateCorrectIbanForSpain() {
        // given
        String deterministicKey = "0123456789";
        String bankCode = "2100";
        String branchCode = "0418";
        String checkChars = "00";
        String ibanPattern =
                CountryCode.ES + "\\d{2}" + bankCode + branchCode + checkChars + deterministicKey;

        // when
        String iban = DemoAccountDefinitionGenerator.generateAccountNumbersES(deterministicKey);

        // then
        Assert.assertTrue(iban.matches(ibanPattern));
    }
}
