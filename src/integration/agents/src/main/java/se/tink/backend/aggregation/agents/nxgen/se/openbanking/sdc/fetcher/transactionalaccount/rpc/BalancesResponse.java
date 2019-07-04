package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.rpc;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.entity.balance.BalanceAccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.entity.balance.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalancesResponse {

    private BalanceAccountDetailsEntity account;
    private List<BalanceEntity> balances;

    public List<BalanceEntity> getBalances() {
        return Optional.ofNullable(balances).orElse(Lists.newArrayList());
    }

    public Amount getBalance() {
        return getBalances().stream()
                .filter(BalanceEntity::isAvailable)
                .findAny()
                .map(balanceEntity -> balanceEntity.getBalanceAmount().toAmount())
                .orElseGet(() -> Amount.inSEK(0));
    }
}
