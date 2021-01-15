package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities.HolderEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.ListHoldersResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.rpc.LaCaixaErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class LaCaixaAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final LaCaixaApiClient apiClient;

    public LaCaixaAccountFetcher(LaCaixaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        try {
            return Optional.ofNullable(apiClient.fetchAccountList())
                    .filter(ListAccountsResponse::hasAccounts)
                    .map(this::getTransactionalAccounts)
                    .orElseGet(Collections::emptyList);
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            if (response.getStatus() == HttpStatus.SC_CONFLICT) {
                LaCaixaErrorResponse errorResponse = response.getBody(LaCaixaErrorResponse.class);
                if (errorResponse.isNoAccounts()) {
                    return Collections.emptyList();
                }
            }
            throw e;
        }
    }

    private List<Holder> fetchHoldersForAccount(AccountEntity accountEntity) {
        ListHoldersResponse listHoldersResponse =
                apiClient.fetchHolderList(accountEntity.getIdentifiers().getAccountReference());
        return HolderEntity.toTinkHolders(listHoldersResponse);
    }

    private Collection<TransactionalAccount> getTransactionalAccounts(
            ListAccountsResponse accountResponse) {
        return accountResponse.getAccounts().stream()
                .map(account -> account.toTinkAccount(fetchHoldersForAccount(account)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
