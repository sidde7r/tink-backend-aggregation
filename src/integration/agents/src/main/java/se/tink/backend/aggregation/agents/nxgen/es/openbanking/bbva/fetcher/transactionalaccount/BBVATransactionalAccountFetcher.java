package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BBVAApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.rpc.BBVADetailedAccountResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class BBVATransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final BBVAApiClient apiClient;

    public BBVATransactionalAccountFetcher(BBVAApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts()
            .getData().getAccounts().stream()
            .map(accountEntity -> {
                BBVADetailedAccountResponse bbvaDetailedAccountResponse = apiClient
                    .fetchAccountDetails(accountEntity.getId());

                return bbvaDetailedAccountResponse.toTinkAccount();
            }).collect(Collectors.toList());
    }
}
