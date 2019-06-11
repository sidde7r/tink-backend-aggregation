package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesResponse {

    private List<Balance> balances;

    public List<Balance> getBalances() {
        return balances;
    }
}
