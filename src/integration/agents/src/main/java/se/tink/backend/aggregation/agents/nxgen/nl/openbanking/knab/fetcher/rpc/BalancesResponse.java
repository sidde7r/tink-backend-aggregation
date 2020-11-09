package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.rpc;

import java.util.List;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalancesResponse {
    private AccountEntity account;
    private List<BalanceEntity> balances;

    public ExactCurrencyAmount getBalance() {
        return ListUtils.emptyIfNull(balances).stream()
                .filter(BalanceEntity::isInterimBooked)
                .findAny()
                .map(balanceEntity -> balanceEntity.getBalanceAmount().toTinkAmount())
                .orElseGet(this::getDefaultAmount);
    }

    private ExactCurrencyAmount getDefaultAmount() {
        return ExactCurrencyAmount.zero(account.getCurrency());
    }
}
