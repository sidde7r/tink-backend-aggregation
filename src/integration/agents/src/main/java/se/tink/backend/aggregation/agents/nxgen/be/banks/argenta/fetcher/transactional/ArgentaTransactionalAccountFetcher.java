package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.entity.ArgentaAccount;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.rpc.ArgentaAccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.agents.rpc.AccountTypes;

public class ArgentaTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final ArgentaApiClient apiClient;
    private final String deviceId;

    public ArgentaTransactionalAccountFetcher(ArgentaApiClient apiClient, String deviceId) {
        this.apiClient = apiClient;
        this.deviceId = deviceId;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        Collection<TransactionalAccount> accounts = new ArrayList<>();
        ArgentaAccountResponse response;
        int page = 1;

        do {
            response = apiClient.fetchAccounts(page, deviceId);
            page = response.getNextPage();

            List<TransactionalAccount> accountsPage =
                    response.getAccounts()
                            .stream()
                            .map(ArgentaAccount::toTransactionalAccount)
                            .filter(
                                    account ->
                                            account.getType().equals(AccountTypes.CHECKING)
                                                    || account.getType().equals(AccountTypes.SAVINGS))
                            .collect(Collectors.toList());
            accounts.addAll(accountsPage);
        } while (page != 0);

        return accounts;
    }
}
