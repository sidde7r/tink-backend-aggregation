package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.FinnishIdentifier;
import se.tink.libraries.amount.Amount;

public class HandelsbankenFIAccount extends HandelsbankenAccount {

    private HandelsbankenAmount balance;
    private HandelsbankenAmount amount;
    private String displayName;
    private String number;
    private String unformattedNumber;

    public TransactionalAccount toTinkAccount() {
        return CheckingAccount.builder(
                        unformattedNumber, Amount.inEUR(chooseAmountField().asDouble()))
                .setAccountNumber(number)
                .setName(displayName)
                .addIdentifier(new FinnishIdentifier(unformattedNumber))
                .setBankIdentifier(unformattedNumber)
                .build();
    }

    private HandelsbankenAmount chooseAmountField() {
        return amount != null ? amount : balance;
    }

    @Override
    public boolean is(Account account) {
        return account.isUniqueIdentifierEqual(unformattedNumber);
    }
}
