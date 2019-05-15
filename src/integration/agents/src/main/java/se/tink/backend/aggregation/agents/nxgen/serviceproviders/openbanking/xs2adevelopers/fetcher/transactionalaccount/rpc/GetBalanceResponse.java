package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetBalanceResponse {
    private List<BalanceEntity> balances;

    public List<BalanceEntity> getBalances() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList());
    }
}
