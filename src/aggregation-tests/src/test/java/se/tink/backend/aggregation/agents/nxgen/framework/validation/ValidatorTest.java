package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
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
                                acc -> String.format("Account %s is null", acc))
                        .ruleAccount(
                                "Balance does not exceed threshold",
                                acc -> acc.getBalance() <= 10000000.0,
                                acc ->
                                        String.format(
                                                "Balance %f exceeds threshold %f for account %s",
                                                acc.getBalance(), 10000000.0, acc))
                        .ruleAccount(
                                "Holder name is present",
                                acc -> acc.getHolderName() != null,
                                acc -> String.format("Account lacks a holder name: %s", acc))
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

    @Test
    public void testExtensiveValidator() {
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

        AisValidator validator = ValidatorFactory.getExtensiveValidator();

        validator.validate(accounts, transactions);
    }
}
