package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchAccountsResponse {

    private List<AccountEntity> accounts;

    public Collection<AccountEntity> getAccounts() {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .filter(l -> l.getCurrency().equalsIgnoreCase("EUR"))
                .collect(Collectors.toList());
    }
}
