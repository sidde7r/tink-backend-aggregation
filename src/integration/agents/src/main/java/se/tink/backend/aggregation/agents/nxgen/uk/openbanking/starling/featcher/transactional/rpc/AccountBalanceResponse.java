package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc;

import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;

@Getter
@JsonObject
public class AccountBalanceResponse {

    private AmountEntity clearedBalance;
    private AmountEntity effectiveBalance;
    private AmountEntity pendingTransactions;
    private AmountEntity availableToSpend;
    private AmountEntity acceptedOverdraft;
    private AmountEntity amount;

    public static BalanceModule createBalanceModule(AccountBalanceResponse balance) {
        BalanceBuilderStep balanceBuilder;

        if (balance.getClearedBalance() != null) {
            balanceBuilder =
                    BalanceModule.builder()
                            .withBalance(balance.getClearedBalance().toExactCurrencyAmount());
        } else {
            throw new AccountRefreshException("Balance cannot be found.");
        }
        Optional.ofNullable(balance.getEffectiveBalance())
                .map(AmountEntity::toExactCurrencyAmount)
                .ifPresent(balanceBuilder::setAvailableBalance);

        return balanceBuilder.build();
    }
}
