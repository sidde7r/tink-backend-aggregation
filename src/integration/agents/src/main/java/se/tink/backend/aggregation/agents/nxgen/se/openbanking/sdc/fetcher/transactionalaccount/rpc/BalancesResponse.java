package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.rpc;

import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.entity.balance.BalanceAccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.entity.balance.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalancesResponse {

    private BalanceAccountDetailsEntity account;
    private List<BalanceEntity> balances;

    public List<BalanceEntity> getBalances() {
        return Optional.ofNullable(balances).orElse(Lists.emptyList());
    }

    public Amount getBalance() {
        return getBalances().stream()
                .filter(BalanceEntity::isAvailable)
                .findAny()
                .map(balanceEntity -> balanceEntity.getBalanceAmount().toAmount())
                .orElseGet(() -> Amount.inSEK(0));
    }
}
