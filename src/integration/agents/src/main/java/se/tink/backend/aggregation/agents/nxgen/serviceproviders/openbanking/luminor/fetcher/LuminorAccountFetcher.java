package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
@JsonObject
@RequiredArgsConstructor
public class LuminorAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final LuminorApiClient apiClient;
    private final PersistentStorage persistentStorage;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountsResponse response = apiClient.getAccounts();
        String accountHolderName = findAccountHolderName(response);
        return response.getAccounts().stream()
                .filter(AccountEntity::isEUR)
                .map(accountEntity -> accountEntity.toTinkAccount(accountHolderName))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public String getAccountHolderName(String accountId) {
        LocalDate fromDate = LocalDate.now().minusDays(7);
        LocalDate toDate = LocalDate.now();

        TransactionsResponse response =
                apiClient.getTransactions(accountId, fromDate.toString(), toDate.toString());

        return Optional.ofNullable(response)
                .map(TransactionsResponse::getAccountHolderName)
                .orElse(null);
    }

    @JsonIgnore
    private String findAccountHolderName(AccountsResponse accountsResponse) {
        String accountId;
        String name = null;
        int numOfAccounts = accountsResponse.getAccounts().size();
        for (int i = 0; i < numOfAccounts; i++) {
            accountId = accountsResponse.getAccounts().get(i).getResourceId();
            name = getAccountHolderName(accountId);
            if (name != null) {
                persistentStorage.put(StorageKeys.FULL_NAME, name);
                return name;
            }
        }
        return name;
    }
}
