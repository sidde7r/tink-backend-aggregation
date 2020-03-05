package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.CLOSING_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.CLOSING_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.EXPECTED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.INTERIM_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.INTERIM_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.OPENING_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.OPENING_BOOKED;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.PrioritizedValueExtractor;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RequiredArgsConstructor
public class TransactionalAccountBalanceMapper {

    private final PrioritizedValueExtractor valueExtractor;

    ExactCurrencyAmount getAccountBalance(Collection<AccountBalanceEntity> balances) {
        return valueExtractor
                .pickByValuePriority(
                        balances,
                        AccountBalanceEntity::getType,
                        ImmutableList.of(
                                OPENING_AVAILABLE,
                                CLOSING_AVAILABLE,
                                INTERIM_AVAILABLE,
                                OPENING_BOOKED,
                                CLOSING_BOOKED,
                                INTERIM_BOOKED,
                                EXPECTED))
                .getAmount();
    }
}
