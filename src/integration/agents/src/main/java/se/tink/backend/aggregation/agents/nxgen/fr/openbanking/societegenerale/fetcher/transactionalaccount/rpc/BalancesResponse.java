package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities.BalancesItemEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities.LinksBalancesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesResponse {

    private List<BalancesItemEntity> balances;

    @JsonProperty("_links")
    private LinksBalancesEntity linksBalancesEntity;

    public List<BalancesItemEntity> getBalances() {
        return balances;
    }

    public LinksBalancesEntity getLinksBalancesEntity() {
        return linksBalancesEntity;
    }
}
