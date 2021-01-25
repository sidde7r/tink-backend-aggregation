package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.entity.ArgentaAccount;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.rpc.ArgentaAccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class ArgentaTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final ArgentaApiClient apiClient;
    private final ArgentaPersistentStorage persistentStorage;

    public ArgentaTransactionalAccountFetcher(
            ArgentaApiClient apiClient, ArgentaPersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        Collection<TransactionalAccount> accounts = new ArrayList<>();
        ArgentaAccountResponse response;
        int page = 1;
        String deviceId = this.persistentStorage.getDeviceId();

        do {
            response = apiClient.fetchAccounts(page, deviceId);
            page = response.getNextPage();

            response.getAccounts().stream()
                    .map(ArgentaAccount::toTransactionalAccount)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(this::onlySavings)
                    .forEach(accounts::add);

        } while (page != 0);

        return accounts;
    }

    private boolean onlySavings(TransactionalAccount account) {
        return AccountTypes.SAVINGS == account.getType();
    }
}
