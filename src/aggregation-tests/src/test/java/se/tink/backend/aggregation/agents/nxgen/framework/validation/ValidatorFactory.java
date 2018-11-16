package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
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
                throw new AssertionError("Unexpected number of elements"); // Should never happen
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
                        "Account name is present",
                        acc -> acc.getName() != null,
                        acc -> String.format("Account lacks a name: %s", acc))
                .ruleTransaction(
                        "Transaction description is present",
                        trx -> trx.getDescription() != null,
                        trx -> String.format("Transaction description is null: %s", trx))
                .ruleTransaction(
                        "Transaction description length is reasonable",
                        trx -> Optional.ofNullable(trx.getDescription()).map(String::length).orElse(0) <= 1000,
                        trx -> String.format("Transaction description is too long: %s", trx))
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
