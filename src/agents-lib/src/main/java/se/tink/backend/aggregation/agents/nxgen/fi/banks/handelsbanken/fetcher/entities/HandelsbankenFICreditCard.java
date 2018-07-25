package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities.HandelsbankenCreditCard;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.URL;

public class HandelsbankenFICreditCard extends HandelsbankenCreditCard {

    private HandelsbankenAmount amountAvailable;
    private HandelsbankenAmount creditLimit;
    private String name;
    private String numberMasked;

    public CreditCardAccount toTinkAccount() {
        return CreditCardAccount.builder(numberMasked, amountAvailable.asAmount(), creditLimit.asAmount())
                .setAccountNumber(numberMasked)
                .setBankIdentifier(numberMasked)
                .setName(name)
                .build();
    }

    @Override
    public URL toCardTransactions() {
        return findLink(HandelsbankenConstants.URLS.Links.TRANSINFO);
    }

    @Override
    public boolean is(Account account) {
        return numberMasked != null && numberMasked.equals(account.getBankIdentifier());
    }
}
