package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class SwedbankTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final SwedbankApiClient apiClient;

    public SwedbankTransactionalAccountFetcher(SwedbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        return apiClient.fetchAccounts().getAccountList().stream()
                .filter(filterInvalidAccounts()) // TODO remove this filter for prod
                .map(toTinkAccountWithBalance())
                .collect(Collectors.toList());
    }

    private Function<AccountEntity, TransactionalAccount> toTinkAccountWithBalance() {
        return account -> {
            AccountBalanceResponse accountBalanceResponse =
                    apiClient.getAccountBalance(account.getId());

            return account.toTinkAccount(accountBalanceResponse);
        };
    }

    private Predicate<AccountEntity> filterInvalidAccounts() { // Sandbox limitation
        return accountEntity ->
                !accountEntity.getId().equalsIgnoreCase("Baas786DD5886RT")
                        && !accountEntity.getId().equalsIgnoreCase("458A889B8889T784W");
    }
}
