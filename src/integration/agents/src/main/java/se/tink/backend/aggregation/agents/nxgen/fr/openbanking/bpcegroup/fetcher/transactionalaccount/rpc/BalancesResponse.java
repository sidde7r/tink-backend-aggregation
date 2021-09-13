package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.accounts.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class BalancesResponse {

    private List<BalanceEntity> balances;

    public List<BalanceEntity> getBalances() {
        return Optional.ofNullable(balances).orElseGet(Collections::emptyList);
    }
}
