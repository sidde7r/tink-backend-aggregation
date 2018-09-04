package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.AccountListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

public class HandelsbankenCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {
    private final HandelsbankenApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;

    public HandelsbankenCreditCardAccountFetcher(HandelsbankenApiClient client,
            HandelsbankenSessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<CreditCardAccount> fetchAccounts() {
        // Credit cards that are sent through the account listing.
        List<CreditCardAccount> creditCardAccounts = sessionStorage.applicationEntryPoint().map(applicationEntryPoint -> {
                    AccountListResponse accountList = client.accountList(applicationEntryPoint);
                    sessionStorage.persist(accountList);
                    return accountList.toTinkCreditCard(client, applicationEntryPoint).collect(Collectors.toList());
                }
        ).orElse(Collections.emptyList());

        creditCardAccounts.addAll(
            sessionStorage.applicationEntryPoint().map(applicationEntryPointResponse -> {
                        CreditCardsResponse cards = client.creditCards(applicationEntryPointResponse);
                        sessionStorage.persist(cards);
                        return cards.toTinkAccounts(client);
                    }
            ).orElse(Collections.emptyList())
        );

        return creditCardAccounts;
    }
}
