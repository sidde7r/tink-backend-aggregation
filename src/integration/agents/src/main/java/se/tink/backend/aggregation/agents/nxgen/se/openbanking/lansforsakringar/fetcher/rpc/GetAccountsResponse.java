package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@Getter
@JsonObject
public class GetAccountsResponse {

    private List<AccountEntity> accounts;

    public Collection<TransactionalAccount> toTinkAccounts(GetBalancesResponse balancesResponse) {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .map(acc -> acc.toTinkAccount(balancesResponse))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
