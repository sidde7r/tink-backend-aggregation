package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities.BalancesItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities.LinksBalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceResponse {

    private List<BalancesItemEntity> balances;

    @JsonProperty("_links")
    private LinksBalanceEntity linksBalanceEntity;

    public List<BalancesItemEntity> getBalances() {
        return balances;
    }

    public LinksBalanceEntity getLinksBalanceEntity() {
        return linksBalanceEntity;
    }
}
