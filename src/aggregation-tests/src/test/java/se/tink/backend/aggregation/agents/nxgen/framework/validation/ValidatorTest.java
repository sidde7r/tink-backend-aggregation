package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import java.util.Collections;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.backend.aggregation.rpc.Account;

// TODO Move to one of the test jars
public final class ValidatorTest {
    @Test
    public void testValidator() {
        Account account = new Account();
        account.setBalance(999999999.0);

        AisData aisData = new AisData(Collections.singleton(account), Collections.emptySet());

        final SilentAction action = new SilentAction();

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
}
