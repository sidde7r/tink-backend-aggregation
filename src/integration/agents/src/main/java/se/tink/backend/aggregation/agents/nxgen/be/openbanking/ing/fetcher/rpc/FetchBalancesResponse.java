package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities.BalancesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchBalancesResponse {

    private List<BalancesEntity> balances;

    public List<BalancesEntity> getBalances() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList());
    }
}
