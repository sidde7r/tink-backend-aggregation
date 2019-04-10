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

    public Collection<AccountEntity> getAccounts(String currency) {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .collect(Collectors.groupingBy(AccountEntity::getResourceId))
                .entrySet()
                .stream()
                .flatMap(
                        // TODO: This thing is crazy. Why was it approved?
                        d ->
                                d.getValue().size() > 1
                                        ? d.getValue().stream()
                                                .filter(
                                                        a ->
                                                                a.getCurrency()
                                                                        .equalsIgnoreCase(currency))
                                        : d.getValue().stream())
                .collect(Collectors.toList());
    }
}
