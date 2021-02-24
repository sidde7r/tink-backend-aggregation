package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.entities.HolderEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.rpc.ListHoldersResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class ImaginBankAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final ImaginBankApiClient apiClient;

    public ImaginBankAccountFetcher(ImaginBankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        try {
            return Optional.ofNullable(apiClient.fetchAccounts())
                    .map(this::getTinkAccounts)
                    .orElseGet(Collections::emptyList);
        } catch (HttpResponseException e) {
            throw new AccountRefreshException("Failed to fetch some accounts.", e);
        }
    }

    private List<Party> fetchPartiesForAccount(AccountEntity accountEntity) {
        ListHoldersResponse listHoldersResponse =
                apiClient.fetchHolderList(accountEntity.getIdentifiers().getAccountReference());
        return HolderEntity.toParties(listHoldersResponse);
    }

    public List<TransactionalAccount> getTinkAccounts(AccountsResponse response) {
        if (response.isMoreData()) {
            log.warn("More accounts available. Check if response contains key to the next page.");
        }
        return response.getAccounts().stream()
                .map(account -> account.toTinkAccount(fetchPartiesForAccount(account)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
