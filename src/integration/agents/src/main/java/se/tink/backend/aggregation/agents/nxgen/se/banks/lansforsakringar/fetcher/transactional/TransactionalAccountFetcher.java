package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.entity.MainAndCoAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class TransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final LansforsakringarApiClient apiClient;

    public TransactionalAccountFetcher(LansforsakringarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public Collection<TransactionalAccount> fetchAccounts() {
        final ListAccountsResponse accountsResponse = apiClient.fetchAccounts();
        final List<MainAndCoAccountsEntity> mainAndCoAccounts =
                accountsResponse.getMainAndCoAccounts();

        if (accountsResponse == null) {
            return Collections.emptyList();
        }

        Collection<TransactionalAccount> transAccounts = mainAndCoAccounts.stream()
                .filter(MainAndCoAccountsEntity::isTransactionalAccount)
                .map(MainAndCoAccountsEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return transAccounts;
    }
}
