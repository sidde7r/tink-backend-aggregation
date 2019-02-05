package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.entities;

import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardSETransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenCreditCard;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.amount.Amount;

public class HandelsbankenSECreditCard extends HandelsbankenCreditCard {
    private HandelsbankenAmount amountAvailable;
    private HandelsbankenAmount balance;
    private String name;
    private String numberMasked;
    private String typeCode;

    public CreditCardAccount toTinkCreditAccount(HandelsbankenSEApiClient client) {
        CreditCardSETransactionsResponse cardTransactions = client.creditCardTransactions(this);

        return CreditCardAccount.builder(numberMasked, Amount.inSEK(cardTransactions.findUsedCredit(balance)),
                Amount.inSEK(cardTransactions.findSpendable(amountAvailable)))
                .setAccountNumber(numberMasked)
                .setName(name)
                .setBankIdentifier(numberMasked)
                .build();
    }

    public URL getCardTransactionsUrl() {
        return findLink(HandelsbankenConstants.URLS.Links.CARD_TRANSACTIONS);
    }

    // SHB sends all cards of the user, not 'just' the credit cards.
    // Also sends cards that are "accounts" (typeCode "A")
    public boolean isCreditCard() {
        return !HandelsbankenSEConstants.Fetcher.CREDIT_CARD_IGNORE_TYPE.equalsIgnoreCase(typeCode) &&
                searchLink(HandelsbankenConstants.URLS.Links.CARD_TRANSACTIONS).isPresent();
    }

    @Override
    public boolean is(Account account) {
        return isCreditCard() && numberMasked != null && numberMasked.equals(account.getBankIdentifier());
    }

    public boolean hasInvertedTransactions() {
        return HandelsbankenSEConstants.Fetcher.PROVIDERS_WITH_INVERTED_TRANSACTIONS.contains(typeCode);
    }
}
