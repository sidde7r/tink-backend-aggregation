package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.Accounts;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.Accounts.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.Accounts.entities.OwnAccountsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;


public class IcaBankenAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final IcaBankenApiClient apiClient;

    public IcaBankenAccountFetcher(IcaBankenApiClient apiClient){
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        AccountsEntity userAccounts = apiClient.requestAccountsBody().getAccounts();

        Collection<TransactionalAccount> accounts = userAccounts.getJointAccounts().stream()
                .map(OwnAccountsEntity::toTinkAccount)
                .collect(Collectors.toList());

        accounts.addAll(userAccounts.getOwnAccounts().stream()
                .map(OwnAccountsEntity::toTinkAccount)
                .collect(Collectors.toList()));

        accounts.addAll(userAccounts.getMinorsAccounts().stream()
                .map(OwnAccountsEntity::toTinkAccount)
                .collect(Collectors.toList()));

        return accounts;
    }
}
