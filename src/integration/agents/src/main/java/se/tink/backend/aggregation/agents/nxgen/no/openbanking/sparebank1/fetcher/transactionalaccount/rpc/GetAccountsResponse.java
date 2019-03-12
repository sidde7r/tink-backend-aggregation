
package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.fetcher.transactionalaccount.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class GetAccountsResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    private List<AccountEntity> accounts;

    public Collection<TransactionalAccount> toTinkAccounts() {
        return accounts != null ?
                accounts.stream()
                        .map(AccountEntity::toTinkAccount)
                        .collect(Collectors.toList()) :
                Collections.emptyList();
    }
}
