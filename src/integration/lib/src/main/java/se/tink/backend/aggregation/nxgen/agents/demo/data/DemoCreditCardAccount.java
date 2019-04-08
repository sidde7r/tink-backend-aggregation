package se.tink.backend.aggregation.nxgen.agents.demo.data;

import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

public interface DemoCreditCardAccount {
    String getAccountId();

    String getCreditCardNumber();

    HolderName getNameOnCreditCard();

    String getAccountName();

    double getBalance();

    double getAvailableCredit();
}
