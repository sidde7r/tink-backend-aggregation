package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.transactionalaccount.entity.balance;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetBalancesResponse {

    @JsonProperty("_links")
    private Links links;

    private List<Balance> balances;

    public Links getLinks() {
        return links;
    }

    public List<Balance> getBalances() {
        return balances;
    }
}
