package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.CLOSING_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.FORWARD_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.INTERIM_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.INTERIM_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.PREVIOUSLY_CLOSED_BOOKED;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalLimitType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.CreditLineEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@RequiredArgsConstructor
public class DefaultCreditCardBalanceMapper implements CreditCardBalanceMapper {

    private static final List<AccountBalanceType> PREFERRED_BALANCE_TYPES =
            ImmutableList.of(
                    INTERIM_BOOKED,
                    PREVIOUSLY_CLOSED_BOOKED,
                    INTERIM_AVAILABLE,
                    CLOSING_AVAILABLE,
                    FORWARD_AVAILABLE);

    private static final List<ExternalLimitType> PREFERRED_AVAILABLE_CREDIT_LINES =
            ImmutableList.of(
                    ExternalLimitType.AVAILABLE,
                    ExternalLimitType.PRE_AGREED,
                    ExternalLimitType.CREDIT);

    private final PrioritizedValueExtractor valueExtractor;

    @Override
    public ExactCurrencyAmount getAccountBalance(Collection<AccountBalanceEntity> balances) {
        return valueExtractor
                .pickByValuePriority(
                        balances, AccountBalanceEntity::getType, PREFERRED_BALANCE_TYPES)
                .map(AccountBalanceEntity::getAmount)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Could not extract credit card account balance. No available balance with type of: "
                                                + StringUtils.join(',', PREFERRED_BALANCE_TYPES)));
    }

    @Override
    public ExactCurrencyAmount getAvailableCredit(Collection<AccountBalanceEntity> balances) {
        List<CreditLineEntity> creditLines =
                balances.stream()
                        .flatMap(b -> CollectionUtils.emptyIfNull(b.getCreditLine()).stream())
                        .collect(Collectors.toList());

        return valueExtractor
                .pickByValuePriority(
                        creditLines, CreditLineEntity::getType, PREFERRED_AVAILABLE_CREDIT_LINES)
                .map(CreditLineEntity::getAmount)
                .map(
                        amount ->
                                ExactCurrencyAmount.of(
                                        amount.getUnsignedAmount(), amount.getCurrency()))
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Could not extract available credit. No available credit line with type of: "
                                                + StringUtils.join(
                                                        ',', PREFERRED_AVAILABLE_CREDIT_LINES)));
    }
}
