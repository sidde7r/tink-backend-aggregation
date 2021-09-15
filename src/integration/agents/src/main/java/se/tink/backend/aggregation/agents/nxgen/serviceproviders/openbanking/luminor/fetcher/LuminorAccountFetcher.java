package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@Slf4j
@JsonObject
@RequiredArgsConstructor
public class LuminorAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final LuminorApiClient apiClient;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountsResponse response = apiClient.getAccounts();
        return response.getAccounts().stream()
                .filter(AccountEntity::isEUR)
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
