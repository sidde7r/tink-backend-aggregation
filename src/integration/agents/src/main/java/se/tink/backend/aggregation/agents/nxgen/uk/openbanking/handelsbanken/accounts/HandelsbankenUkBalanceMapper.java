package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.accounts;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.ExceptionMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BalancesItemEntity;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class HandelsbankenUkBalanceMapper {

    public BalanceModule createAccountBalance(List<BalancesItemEntity> balancesItemEntities) {
        BalanceBuilderStep balanceBuilder =
                BalanceModule.builder().withBalance(getCurrentBalance(balancesItemEntities));
        getAvailableBalance(balancesItemEntities).ifPresent(balanceBuilder::setAvailableBalance);
        return balanceBuilder.build();
    }

    private ExactCurrencyAmount getCurrentBalance(Collection<BalancesItemEntity> balances) {
        return balances.stream()
                .filter(BalancesItemEntity::isCurrent)
                .map(BalancesItemEntity::getAmountEntity)
                .map(AmountEntity::toExactCurrencyAmount)
                .findFirst()
                .orElseThrow(
                        () -> new AccountRefreshException(ExceptionMessages.BALANCE_NOT_FOUND));
    }

    private Optional<ExactCurrencyAmount> getAvailableBalance(
            Collection<BalancesItemEntity> balances) {
        return balances.stream()
                .filter(BalancesItemEntity::isAvailable)
                .map(BalancesItemEntity::getAmountEntity)
                .map(AmountEntity::toExactCurrencyAmount)
                .findFirst();
    }
}
