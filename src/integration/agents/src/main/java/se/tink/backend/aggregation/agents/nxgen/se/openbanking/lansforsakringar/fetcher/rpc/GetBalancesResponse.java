package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities.BalanceAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class GetBalancesResponse {

    private List<BalanceEntity> balances;

    public ExactCurrencyAmount getBalance() {
        return balances.stream()
                .filter(BalanceEntity::isAvailableBalance)
                .findFirst()
                .map(BalanceEntity::getBalanceAmount)
                .map(BalanceAmountEntity::getAmount)
                .orElseThrow(() -> new IllegalStateException("No balance found in the response"));
    }
}
