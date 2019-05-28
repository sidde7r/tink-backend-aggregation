package se.tink.backend.aggregation.nxgen.agents.demo.data;

import java.util.List;
import se.tink.libraries.account.AccountIdentifier;

public interface DemoSavingsAccount {
    String getAccountId();

    String getAccountName();

    double getAccountBalance();

    List<AccountIdentifier> getIdentifiers();
}
