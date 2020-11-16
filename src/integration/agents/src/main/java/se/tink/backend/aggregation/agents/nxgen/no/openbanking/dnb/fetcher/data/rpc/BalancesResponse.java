package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.Balance;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class BalancesResponse {

    private List<Balance> balances = Collections.emptyList();

    public Optional<Balance> getBalanceOfType(String type) {
        return balances.stream().filter(x -> type.equalsIgnoreCase(x.getBalanceType())).findFirst();
    }
}
