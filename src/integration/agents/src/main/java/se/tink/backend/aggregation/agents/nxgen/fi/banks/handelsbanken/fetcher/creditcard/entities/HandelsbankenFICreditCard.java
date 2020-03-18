package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.creditcard.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenCreditCard;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class HandelsbankenFICreditCard extends HandelsbankenCreditCard {

    private HandelsbankenAmount amountAvailable;
    private HandelsbankenAmount creditLimit;
    private String name;
    private String numberMasked;

    public CreditCardAccount toTinkAccount() {
        ExactCurrencyAmount credLimit = creditLimit.toExactCurrencyAmount();
        ExactCurrencyAmount credAvailable = amountAvailable.toExactCurrencyAmount();
        // calculate balance, nordea-presented as positive value, negate
        ExactCurrencyAmount balance = credLimit.subtract(credAvailable).negate();

        return CreditCardAccount.builder(numberMasked, balance, credAvailable)
                .setAccountNumber(numberMasked)
                .setBankIdentifier(numberMasked)
                .setName(name)
                .build();
    }

    @Override
    public URL getCardTransactionsUrl() {
        return findLink(HandelsbankenConstants.URLS.Links.TRANSINFO);
    }

    @Override
    public boolean is(Account account) {
        return numberMasked != null && numberMasked.equals(account.getApiIdentifier());
    }
}
