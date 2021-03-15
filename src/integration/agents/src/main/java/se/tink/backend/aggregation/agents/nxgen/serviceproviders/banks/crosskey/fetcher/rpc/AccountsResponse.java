package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities.CrossKeyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.rpc.CrossKeyResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountsResponse extends CrossKeyResponse {

    private List<CrossKeyAccount> accounts;

    public Collection<TransactionalAccount> getTransactionalAccounts(
            CrossKeyConfiguration agentConfiguration) {
        return Optional.ofNullable(accounts).orElseGet(Collections::emptyList).stream()
                .filter(CrossKeyAccount::isTransactionalAccount)
                .map(account -> account.toTransactionalAccount(agentConfiguration))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<CrossKeyAccount> getAccounts() {
        return accounts;
    }
}
