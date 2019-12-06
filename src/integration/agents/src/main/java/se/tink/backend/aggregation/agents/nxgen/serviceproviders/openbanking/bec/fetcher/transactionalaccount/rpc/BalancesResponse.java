package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.entities.BalancesItemEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesResponse {

    private List<BalancesItemEntity> balances;

    private AccountEntity account;

    public BalancesItemEntity getBalance() {
        return balances.stream()
                .sorted()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No balances found"));
    }
}
