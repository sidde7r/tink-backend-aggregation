package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import java.util.stream.Collectors;
import se.tink.backend.aggregation.rpc.Account;
import java.util.Collection;
import se.tink.backend.system.rpc.Transaction;

public final class ValidatorFactory {
    private ValidatorFactory() {
        throw new AssertionError();
    }

    public static AisValidator getExtensiveValidator() {
        return AisValidator.builder()
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
                        aisdata ->
                                aisdata.getAccounts()
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
    }
}
