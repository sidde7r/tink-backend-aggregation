package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.balance.BalancesItem;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountBalanceResponse {

    private List<BalancesItem> balances;

    public List<BalancesItem> getBalances() {
        return Optional.of(balances).orElseGet(Lists::newArrayList);
    }
}
