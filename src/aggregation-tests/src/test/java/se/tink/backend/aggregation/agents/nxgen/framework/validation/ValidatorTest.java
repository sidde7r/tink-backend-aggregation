package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.system.rpc.Transaction;

// TODO Move to one of the test jars
public final class ValidatorTest {
    @Test
    public void testValidator() {
        Account account = new Account();
        account.setBalance(999999999.0);
        account.setHolderName("Test Testsson");

        AisData aisData = new AisData(Collections.singleton(account), Collections.emptySet());

        SilentAction action = new SilentAction();

        AisValidator validator =
                AisValidator.builder()
                        .setAction(action)
                        .ruleAccount(
                                "Account number is non-null",
                                acc -> acc.getAccountNumber() != null,
                                acc -> String.format("Account number of %s is null", acc))
                        .ruleTransaction(
                                "Transaction description is present",
                                trx -> trx.getDescription() != null,
                                trx -> String.format("Transaction description is null: %s", trx))
                        .rule(
                                "Account balance threshold",
                                data ->
                                        data.getAccounts()
                                                .stream()
                                                .map(Account::getBalance)
                                                .allMatch(b -> b <= 10000000.0),
                                data ->
                                        String.format(
                                                "One of the balances in %s exceed 10000000.0",
                                                data.getAccounts()
                                                        .stream()
                                                        .map(Account::getBalance)
                                                        .collect(Collectors.toList())))
                        .build();

        validator.validate(aisData);
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

    @Test
    public void testExtensiveValidator() {
        // Arrange data to be validated
        Collection<Account> accounts = Collections.emptySet();

        List<Transaction> transactions =
                ImmutableList.<Transaction>builder()
                        .add(new Transaction())
                        .add(new Transaction())
                        .build();

        for (Transaction t : transactions) {
            t.setAccountId("1234");
            t.setAmount(7.0);
            t.setDate(new Date(1234567890));
            t.setDescription("my description");
        }

        // Arrange validator
        SilentAction action = new SilentAction();
        AisValidator validator =
                ValidatorFactory.getExtensiveValidator().rebuilder().setAction(action).build();

        // Act
        validator.validate(accounts, transactions);

        // Assert
        Assert.assertEquals(action.getOnFailAisData().size(), 1);
        Assert.assertEquals("No duplicate transactions",
        action.getOnFailAisData().get(0).second);
    }
}
