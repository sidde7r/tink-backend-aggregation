package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.rpc.CreditCardsResponse;
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
        return sessionStorage.applicationEntryPoint().map(applicationEntryPointResponse -> {
                    CreditCardsResponse cards = client.creditCards(applicationEntryPointResponse);
                    sessionStorage.persist(cards);
                    return cards.toTinkAccounts(client);
                }
        ).orElse(Collections.emptyList());
    }
}
