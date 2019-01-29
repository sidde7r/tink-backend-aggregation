package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.creditcard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFIApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardsFIResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.AccountListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class HandelsbankenFICreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private final HandelsbankenFIApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;

    public HandelsbankenFICreditCardAccountFetcher(HandelsbankenFIApiClient client,
            HandelsbankenSessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        // Credit cards that are sent through the account listing.
        List<CreditCardAccount> creditCardAccounts = new ArrayList<>();

        creditCardAccounts.addAll(sessionStorage.applicationEntryPoint()
                .map(applicationEntryPoint -> {
                            AccountListResponse accountList = client.accountList(applicationEntryPoint);
                            sessionStorage.persist(accountList);
                            return accountList.toTinkCreditCard(client).collect(Collectors.toList());
                        }
                ).orElse(Collections.emptyList()));

        creditCardAccounts.addAll(
                sessionStorage.applicationEntryPoint().map(applicationEntryPointResponse -> {
                            CreditCardsFIResponse cards = client.creditCards(applicationEntryPointResponse);
                            sessionStorage.persist(cards);
                            return cards.toTinkAccounts();
                        }
                ).orElse(Collections.emptyList())
        );

        return creditCardAccounts;
    }
}
