package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.AccountResourceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.AccountsPageLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchAccountsResponse {
    @JsonProperty("accounts")
    private List<AccountResourceEntity> accounts = new ArrayList<AccountResourceEntity>();

    @JsonProperty("_links")
    private AccountsPageLinksEntity links = null;

    public List<AccountResourceEntity> getAccounts() {
        return accounts;
    }

    public AccountsPageLinksEntity getLinks() {
        return links;
    }
}
