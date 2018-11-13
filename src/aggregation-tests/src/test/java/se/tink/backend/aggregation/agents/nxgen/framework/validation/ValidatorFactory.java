package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import java.util.stream.Collectors;
import se.tink.backend.aggregation.rpc.Account;

public final class ValidatorFactory {
    private ValidatorFactory() {
        throw new AssertionError();
    }

    public static AisValidator getExtensiveValidator() {
        return AisValidator.builder()
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
