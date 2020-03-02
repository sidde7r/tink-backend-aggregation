package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.CLOSING_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.INTERIM_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.PREVIOUSLY_CLOSED_BOOKED;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalLimitType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.entities.CreditLineEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountBalanceEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RequiredArgsConstructor
public class DefaultCreditCardBalanceMapper implements CreditCardBalanceMapper {

    private final PrioritizedValueExtractor valueExtractor;

    @Override
    public AccountBalanceEntity getAccountBalance(Collection<AccountBalanceEntity> balances) {
        return valueExtractor.pickByValuePriority(
                balances,
                AccountBalanceEntity::getType,
                ImmutableList.of(INTERIM_BOOKED, PREVIOUSLY_CLOSED_BOOKED, CLOSING_AVAILABLE));
    }

    @Override
    public ExactCurrencyAmount getAvailableCredit(Collection<AccountBalanceEntity> balances) {
        List<CreditLineEntity> creditLines =
                balances.stream()
                        .flatMap(b -> CollectionUtils.emptyIfNull(b.getCreditLine()).stream())
                        .collect(Collectors.toList());

        return valueExtractor
                .pickByValuePriority(
                        creditLines,
                        CreditLineEntity::getType,
                        ImmutableList.of(
                                ExternalLimitType.AVAILABLE,
                                ExternalLimitType.PRE_AGREED,
                                ExternalLimitType.CREDIT))
                .getAmount();
    }
}
