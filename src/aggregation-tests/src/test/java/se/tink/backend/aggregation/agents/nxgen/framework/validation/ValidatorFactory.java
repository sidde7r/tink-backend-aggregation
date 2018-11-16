package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import java.util.Collection;
import java.util.Objects;
import se.tink.backend.system.rpc.Transaction;

public final class ValidatorFactory {
    private ValidatorFactory() {
        throw new AssertionError();
    }

    private static boolean containsDuplicates(final Collection<Transaction> transactions) {
        for (final Transaction transaction : transactions) {
            final long numberOfIdenticalElements =
                    transactions
                            .stream()
                            .filter(t -> Objects.equals(t.getDate(), transaction.getDate()))
                            .filter(t -> Objects.equals(t.getDescription(), transaction.getDescription()))
                            .filter(t -> t.getAmount() == transaction.getAmount())
                            .filter(t -> Objects.equals(t.getAccountId(), transaction.getAccountId()))
                            .count();
            if (numberOfIdenticalElements > 1) {
                return false;
            } else if (numberOfIdenticalElements < 1) {
                throw new AssertionError("Unexpected number of elements");
            }
        }
        return true;
    }

    public static AisValidator getExtensiveValidator() {
        return AisValidator.builder()
                .ruleAccount(
                        "Account number is present",
                        acc -> acc.getAccountNumber() != null,
                        acc -> String.format("Account lacks an account number: %s", acc))
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
                        "No duplicate transactions",
                        aisdata -> containsDuplicates(aisdata.getTransactions()),
                        data ->
                                String.format(
                                        "Found at least two transactions with the same date, description, amount and account ID: %s",
                                        data.getTransactions()))
                .build();
    }
}
