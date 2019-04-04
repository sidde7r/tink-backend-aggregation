package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.rpc.BbvaDetailedAccountResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class BbvaTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final BbvaApiClient apiClient;

    public BbvaTransactionalAccountFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getData().getAccounts().stream()
                .filter(this::isCheckingAccount)
                .map(
                        accountEntity -> {
                            BbvaDetailedAccountResponse bbvaDetailedAccountResponse =
                                    apiClient.fetchAccountDetails(accountEntity.getId());

                            return bbvaDetailedAccountResponse.toTinkAccount();
                        })
                .collect(Collectors.toList());
    }

    private boolean isCheckingAccount(AccountEntity account) {
        return BbvaConstants.ACCOUNT_TYPE_MAPPER
                .translate(account.getType())
                .map(item -> item == AccountTypes.CHECKING)
                .orElse(false);
    }
}
