package se.tink.backend.aggregation.nxgen.agents.demo.data;

import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.amount.Amount;

public interface DemoCreditCardAccount {
    String getAccountId();
    String getCreditCardNumber();
    HolderName getNameOnCreditCard();
    String getAccountName();
    Amount getBalance();
    Amount getAvailableCredit();
}
