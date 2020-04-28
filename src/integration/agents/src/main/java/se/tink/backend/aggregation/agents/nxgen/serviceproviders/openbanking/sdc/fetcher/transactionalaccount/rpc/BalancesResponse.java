package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.rpc;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.entity.balance.BalanceAccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.entity.balance.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalancesResponse {

    private BalanceAccountDetailsEntity account;
    private List<BalanceEntity> balances;

    public List<BalanceEntity> getBalances() {
        return Optional.ofNullable(balances).orElse(Lists.newArrayList());
    }

    public ExactCurrencyAmount getBalance() {
        return getBalances().stream()
                .filter(BalanceEntity::isAvailable)
                .findFirst()
                .map(balanceEntity -> balanceEntity.getBalanceAmount().toAmount())
                .orElseGet(() -> ExactCurrencyAmount.inSEK(0));
    }
}
