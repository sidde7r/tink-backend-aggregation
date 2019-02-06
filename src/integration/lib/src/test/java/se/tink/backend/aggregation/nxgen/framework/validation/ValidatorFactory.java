package se.tink.backend.aggregation.nxgen.framework.validation;

import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;

public final class ValidatorFactory {
    private ValidatorFactory() {
        throw new AssertionError();
    }

    public static AisValidator getEmptyValidator() {
        return AisValidator.builder().build();
    }

    public static AisValidator getExtensiveValidator() {
        final DuplicateTransactionFinder dupeFinder = new DuplicateTransactionFinder();

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
                        "Checking account balance is >= 0",
                        acc -> acc.getType() != AccountTypes.CHECKING || acc.getBalance() >= 0)
                .ruleAccount(
                        "Savings account balance is >= 0",
                        acc -> acc.getType() != AccountTypes.SAVINGS || acc.getBalance() >= 0)
                .ruleAccount(
                        "Account name is present",
                        acc -> acc.getName() != null,
                        acc -> String.format("Account lacks a name: %s", acc))
                .ruleAccount(
                        "At least one account identifier is specified (e.g. IBAN)",
                        acc -> acc.getIdentifiers().size() >= 1,
                        acc -> String.format("Account lacks identifier(s): %s", acc))
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
                        trx -> String.format("Transaction description is too long: %s", trx))
                .ruleTransaction(
                        "Transaction amount is nonzero",
                        trx -> trx.getAmount() != 0.0,
                        trx -> String.format("Amount of transaction is zero: %s", trx))
                .rule(
                        "No duplicate transactions",
                        aisdata -> !dupeFinder.containsDuplicates(aisdata.getTransactions()),
                        aisdata ->
                                String.format(
                                        "Found at least two transactions with the same date, description, amount and account ID: %s",
                                        dupeFinder.getAnyDuplicates(aisdata.getTransactions())))
                .build();
    }
}
