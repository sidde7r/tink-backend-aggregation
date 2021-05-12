package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.transactionalaccount.rpc.AccountListSEResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class HandelsbankenSETransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount> {
    private final HandelsbankenSEApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public HandelsbankenSETransactionalAccountFetcher(
            HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        return sessionStorage
                .applicationEntryPoint()
                .map(
                        applicationEntryPoint -> {
                            AccountListSEResponse accountList =
                                    client.accountList(applicationEntryPoint);
                            sessionStorage.persist(accountList);
                            return accountList
                                    .toTinkAccounts(client, persistentStorage)
                                    .collect(Collectors.toList());
                        })
                .orElseGet(Collections::emptyList);
    }
}
