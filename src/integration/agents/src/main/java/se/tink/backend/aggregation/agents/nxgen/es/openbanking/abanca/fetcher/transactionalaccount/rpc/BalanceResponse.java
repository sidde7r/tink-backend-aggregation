package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.balance.BalanceDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.balance.BalanceLinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceResponse {

    private BalanceDataEntity data;
    private BalanceLinkEntity links;

    public Amount getBalance() {
        return Optional.ofNullable(data)
                .map(
                        balanceDataEntity ->
                                balanceDataEntity
                                        .getAttributes()
                                        .getAvailableBalance()
                                        .toTinkAmount())
                .orElseGet(() -> Amount.inEUR(0));
    }
}
