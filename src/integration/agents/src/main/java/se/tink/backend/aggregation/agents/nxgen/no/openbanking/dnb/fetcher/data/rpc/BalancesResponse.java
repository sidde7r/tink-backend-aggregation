package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.Balance;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper.DnbBalanceType;
import se.tink.backend.aggregation.annotations.JsonObject;

@Setter
@Getter
@JsonObject
public class BalancesResponse {

    private List<Balance> balances = Collections.emptyList();

    public Optional<Balance> getBalanceOfType(DnbBalanceType type) {
        return balances.stream()
                .filter(balance -> type.getApiValue().equalsIgnoreCase(balance.getBalanceType()))
                .findFirst();
    }
}
