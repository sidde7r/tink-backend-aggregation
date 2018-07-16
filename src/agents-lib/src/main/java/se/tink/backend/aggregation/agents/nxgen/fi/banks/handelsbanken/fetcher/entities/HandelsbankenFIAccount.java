package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.core.Amount;
import se.tink.libraries.account.identifiers.FinnishIdentifier;

public class HandelsbankenFIAccount extends HandelsbankenAccount {

    private HandelsbankenAmount balance;
    private HandelsbankenAmount amount;
    private String displayName;
    private String number;
    private String unformattedNumber;

    public TransactionalAccount toTinkAccount() {
        return CheckingAccount.builder(unformattedNumber, Amount.inEUR(chooseAmountField().asDouble()))
                .setAccountNumber(number)
                .setName(displayName)
                .addIdentifier(new FinnishIdentifier(unformattedNumber))
                .build();
    }

    private HandelsbankenAmount chooseAmountField() {
        return amount != null ? amount : balance;
    }

    @Override
    public boolean is(Account account) {
        return unformattedNumber != null && unformattedNumber.equals(account.getUniqueIdentifier());
    }
}
