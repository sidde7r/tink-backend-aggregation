package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;

// TODO Move to one of the test jars
public final class ValidatorTest {

    private SilentExecutor executor;

    @Before
    public void setUp() {
        executor = new SilentExecutor();
    }

    @Test
    public void ensureGetSubResults_noRulesNoData_returnsEmpty() {
        AisValidator.builder()
                .setExecutor(executor)
                .build()
                .validate(new AisData(Collections.emptySet(), Collections.emptySet()));

        Assert.assertTrue(executor.getResult().getSubResults().isEmpty());
    }

    @Test
    public void ensureGetSubResults_oneAccountRuleNoData_returnsOne() {
        AisValidator.builder()
                .setExecutor(executor)
                .ruleAccount("Hoy", a -> a.getAccountNumber() != null, a -> "failmsg")
                .build()
                .validate(new AisData(Collections.emptySet(), Collections.emptySet()));

        Assert.assertEquals(executor.getResult().getSubResults().size(), 1);
        Assert.assertEquals(executor.getResult().getSubResults().keySet().iterator().next(), "Hoy");
        Assert.assertTrue(executor.getResult().getSubResults().values().iterator().next().passed());
        Assert.assertEquals(
                executor.getResult().getSubResults().values().iterator().next().getMessage(), "");
    }

    @Test
    public void testValidator() {
        // Set up validator
        AisValidator validator =
                AisValidator.builder()
                        .setExecutor(executor)
                        .ruleAccount(
                                "Account number is present",
                                acc -> acc.getAccountNumber() != null,
                                acc -> String.format("Account lacks an account number: %s", acc))
                        .ruleAccount(
                                "Account balance does not exceed threshold",
                                acc -> acc.getBalance() <= 10000000.0,
                                acc ->
                                        String.format(
                                                "Balance %f exceeds threshold %f for account %s",
                                                acc.getBalance(), 10000000.0, acc))
                        .ruleAccount(
                                "Holder name is present",
                                acc -> acc.getHolderName() != null,
                                acc -> String.format("Account lacks a holder name: %s", acc))
                        .ruleAccount(
                                "Checking account balance is >= 0",
                                acc ->
                                        acc.getType() != AccountTypes.CHECKING
                                                || acc.getBalance() >= 0)
                        .ruleAccount(
                                "Savings account balance is >= 0",
                                acc ->
                                        acc.getType() != AccountTypes.SAVINGS
                                                || acc.getBalance() >= 0)
                        .ruleAccount(
                                "Account name is present",
                                acc -> acc.getName() != null,
                                acc -> String.format("Account lacks a name: %s", acc))
                        .ruleTransaction(
                                "Transaction description is present",
                                trx -> trx.getDescription() != null,
                                trx -> String.format("Transaction description is null: %s", trx))
                        .ruleTransaction(
                                "Transaction description length is reasonable",
                                trx ->
                                        Optional.ofNullable(trx.getDescription())
                                                        .map(String::length)
                                                        .orElse(0)
                                                <= 1000,
                                trx ->
                                        String.format(
                                                "Transaction description is too long: %s", trx))
                        .rule(
                                "No duplicate transactions",
                                aisdata ->
                                        !DuplicateTransactionFinder.containsDupes(
                                                aisdata.getTransactions()))
                        .build();

        // Set up validatee
        Account account = new Account();
        account.setBalance(999999999.0);
        account.setHolderName("Test Testsson");

        AisData aisData = new AisData(Collections.singleton(account), Collections.emptySet());

        // Act
        validator.validate(aisData);

        Map<String, ValidationSubResult> subResults = executor.getResult().getSubResults();

        // Assert

        Assert.assertEquals(9, executor.getResult().getSubResults().size());

        Assert.assertFalse(subResults.get("Account number is present").passed());
        Assert.assertFalse(subResults.get("Account balance does not exceed threshold").passed());
        Assert.assertTrue(subResults.get("Holder name is present").passed());
        Assert.assertTrue(subResults.get("Checking account balance is >= 0").passed());
        Assert.assertTrue(subResults.get("Savings account balance is >= 0").passed());
        Assert.assertFalse(subResults.get("Account name is present").passed());
        Assert.assertTrue(subResults.get("Transaction description is present").passed());
        Assert.assertTrue(subResults.get("Transaction description length is reasonable").passed());
        Assert.assertTrue(subResults.get("No duplicate transactions").passed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureRuleAccount_whenSameRuleInsertedTwice_throw() {
        AisValidator.builder()
                .ruleAccount("Validate thing", acc -> acc.getAccountNumber() != null)
                .ruleAccount("Validate thing", acc -> acc.getBalance() <= 10000000.0)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureRuleTransaction_whenSameRuleInsertedTwice_throw() {
        AisValidator.builder()
                .ruleTransaction("Validate thing", trx -> trx.getDescription() != null)
                .ruleTransaction("Validate thing", trx -> trx.getAmount() <= 10000000.0)
                .build();
    }
}
