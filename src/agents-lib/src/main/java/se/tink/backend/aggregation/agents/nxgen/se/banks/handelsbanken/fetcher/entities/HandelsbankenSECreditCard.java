package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities;

import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc.CreditCardSETransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities.HandelsbankenCreditCard;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.core.Amount;

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

    public URL toCardTransactions() {
        return findLink(HandelsbankenConstants.URLS.Links.CARD_TRANSACTIONS);
    }

    // SHB sends all cards of the user, not 'just' the credit cards
    public boolean isCreditCard() {
        return searchLink(HandelsbankenConstants.URLS.Links.CARD_TRANSACTIONS).isPresent();
    }

    @Override
    public boolean is(Account account) {
        return numberMasked != null && numberMasked.equals(account.getBankIdentifier());
    }

    public boolean hasInvertedTransactions() {
        return HandelsbankenSEConstants.Fetcher.PROVIDERS_WITH_INVERTED_TRANSACTIONS.contains(typeCode);
    }
}
