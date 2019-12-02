package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.balance.BalanceAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.balance.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalancesResponse {

    public BalanceAccountEntity account;
    public List<BalanceEntity> balances;

    public List<BalanceEntity> getBalances() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList());
    }

    public ExactCurrencyAmount getBalance() {

        return getBalances().stream()
                .min(Comparator.comparing(BalanceEntity::getBalanceMappingPriority))
                .map(BalanceEntity::getAmount)
                .orElseThrow(
                        () -> new IllegalStateException(ErrorMessages.ACCOUNT_BALANCE_NOT_FOUND));
    }
}
