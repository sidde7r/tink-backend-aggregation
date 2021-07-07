package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities.transactions.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {

    @JsonProperty("_links")
    public LinksEntity linksEntity;

    private List<AccountsItemEntity> accounts;

    public LinksEntity getLinksEntity() {
        return linksEntity;
    }

    public List<AccountsItemEntity> getAccounts() {
        return accounts;
    }
}
