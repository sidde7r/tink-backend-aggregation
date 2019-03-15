package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities.AccountsResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities.GroupHeaderEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

import java.util.Collection;
import java.util.Collections;

@JsonObject
public class GetAccountsResponse {

    @JsonProperty("group_header")
    private GroupHeaderEntity groupHeader;

    private AccountsResponseEntity response;

    public Collection<TransactionalAccount> toTinkAccounts() {
        return response != null ? response.toTinkAccounts() : Collections.emptyList();
    }
}
