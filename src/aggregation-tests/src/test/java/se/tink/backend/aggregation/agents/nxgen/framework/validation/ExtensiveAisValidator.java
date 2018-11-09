package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.system.rpc.Transaction;

public final class ExtensiveAisValidator {
    private final AisValidator validator;

    public ExtensiveAisValidator() {
        validator =
                AisValidator.builder()
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

    public void validate(Collection<Account> accounts, Collection<Transaction> transactions) {
        validator.validate(new AisData(accounts, transactions));
    }
}
