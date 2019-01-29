package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngHelper;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class IngTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final IngApiClient apiClient;
    private final IngHelper ingHelper;

    public IngTransactionalAccountFetcher(IngApiClient apiClient, IngHelper ingHelper) {
        this.apiClient = apiClient;
        this.ingHelper = ingHelper;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return this.ingHelper.retrieveLoginResponse()
                .flatMap(loginResponse ->
                        this.apiClient.fetchAccounts(loginResponse)
                                .map(AccountsResponse::getAccounts)
                                .map(accounts ->
                                        accounts.stream()
                                                .filter(AccountEntity::isDesiredType)
                                                .map(account -> account.toTinkAccount(loginResponse))
                                                .collect(Collectors.toList())
                                )
                )
                .orElseGet(Collections::emptyList);
    }
}
