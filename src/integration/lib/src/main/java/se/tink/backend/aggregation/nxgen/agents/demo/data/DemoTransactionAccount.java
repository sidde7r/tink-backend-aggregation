package se.tink.backend.aggregation.nxgen.agents.demo.data;

import se.tink.libraries.account.AccountIdentifier;

public interface DemoTransactionAccount {
    String getAccountId();

    String getAccountName();

    double getBalance();

    AccountIdentifier getIdentifier();
}
