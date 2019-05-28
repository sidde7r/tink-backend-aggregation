package se.tink.backend.aggregation.nxgen.agents.demo.data;

import java.util.List;
import se.tink.libraries.account.AccountIdentifier;

public interface DemoTransactionAccount {
    String getAccountId();

    String getAccountName();

    double getBalance();

    List<AccountIdentifier> getIdentifiers();
}
