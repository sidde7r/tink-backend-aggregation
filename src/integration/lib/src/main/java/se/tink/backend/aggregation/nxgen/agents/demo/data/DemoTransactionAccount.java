package se.tink.backend.aggregation.nxgen.agents.demo.data;

import java.util.List;
import java.util.Optional;
import se.tink.libraries.account.AccountIdentifier;

public interface DemoTransactionAccount {
    String getAccountId();

    String getAccountName();

    double getBalance();

    List<AccountIdentifier> getIdentifiers();

    default Optional<Double> getAvailableBalance() {
        return Optional.empty();
    }

    default Optional<Double> getCreditLimit() {
        return Optional.empty();
    }
}
