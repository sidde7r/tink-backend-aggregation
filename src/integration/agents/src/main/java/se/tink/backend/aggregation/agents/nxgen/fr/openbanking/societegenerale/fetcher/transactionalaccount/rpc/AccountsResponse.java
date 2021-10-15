package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {

    @JsonProperty("_links")
    private LinksEntity linksEntity;

    @JsonProperty("accounts")
    private List<AccountsItemEntity> accounts;

    public LinksEntity getLinksEntity() {
        return linksEntity;
    }

    public List<AccountsItemEntity> getAccounts() {
        return accounts;
    }
}
