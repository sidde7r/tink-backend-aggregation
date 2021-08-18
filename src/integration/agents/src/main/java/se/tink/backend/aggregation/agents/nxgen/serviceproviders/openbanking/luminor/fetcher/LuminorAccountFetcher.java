package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@Slf4j
@JsonObject
@RequiredArgsConstructor
public class LuminorAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final LuminorApiClient apiClient;
    private final LocalDateTimeSource localDateTimeSource;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountsResponse response = apiClient.getAccounts();
        String accountHolderName = findAccountHolderName(response);
        return response.getAccounts().stream()
                .filter(AccountEntity::isEUR)
                .map(
                        accountEntity ->
                                accountEntity.toTinkAccount(Optional.ofNullable(accountHolderName)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public Optional<String> getAccountHolderName(String accountId) {
        LocalDate toDate =
                localDateTimeSource.getInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate fromDate = toDate.minusDays(7);

        TransactionsResponse response =
                apiClient.getTransactions(accountId, fromDate.toString(), toDate.toString());

        return Optional.ofNullable(response).flatMap(TransactionsResponse::getAccountHolderName);
    }

    @JsonIgnore
    private String findAccountHolderName(AccountsResponse accountsResponse) {
        return accountsResponse.getAccounts().stream()
                .map(account -> getAccountHolderName(account.getResourceId()))
                .findAny()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElse(null);
    }
}
