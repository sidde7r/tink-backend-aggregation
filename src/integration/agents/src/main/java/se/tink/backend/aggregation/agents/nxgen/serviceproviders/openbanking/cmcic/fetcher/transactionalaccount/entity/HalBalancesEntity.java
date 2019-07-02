package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HalBalancesEntity {
    @JsonProperty("balances")
    private List<BalanceResourceEntity> balances = new ArrayList<BalanceResourceEntity>();

    @JsonProperty("_links")
    private BalancesLinksEntity _links = null;

    public List<BalanceResourceEntity> getBalances() {
        return balances;
    }

    public void setBalances(List<BalanceResourceEntity> balances) {
        this.balances = balances;
    }

    public BalancesLinksEntity get_links() {
        return _links;
    }

    public void set_links(BalancesLinksEntity _links) {
        this._links = _links;
    }
}
