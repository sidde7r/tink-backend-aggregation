package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
@Getter
public class AccountsResponse {

    private List<AccountEntity> accounts;

    public Collection<TransactionalAccount> toTinkAccount(SebBalticsApiClient apiClient) {
        return Optional.ofNullable(accounts)
                .map(Collection::stream)
                .orElse(Stream.empty())
                .map(accountEntity -> accountEntity.toTinkAccount(apiClient))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
