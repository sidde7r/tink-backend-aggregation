package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken.accounts;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.ExceptionMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BalancesItemEntity;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class HandelsbankenUkBalanceMapper {

    private static final List<String> PREFERRED_BALANCE_TYPES =
            ImmutableList.of("AVAILABLE_AMOUNT", "CURRENT", "CLEARED");

    public AmountEntity getBalance(Collection<BalancesItemEntity> balances) {
        return new PrioritizedValueExtractor()
                .pickByValuePriority(
                        balances, BalancesItemEntity::getBalanceType, PREFERRED_BALANCE_TYPES)
                .map(BalancesItemEntity::getAmountEntity)
                .orElseThrow(
                        () -> new AccountRefreshException(ExceptionMessages.BALANCE_NOT_FOUND));
    }
}
