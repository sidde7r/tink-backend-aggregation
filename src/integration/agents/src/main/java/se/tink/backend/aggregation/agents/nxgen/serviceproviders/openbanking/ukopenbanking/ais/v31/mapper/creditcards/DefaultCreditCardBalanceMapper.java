package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.CLOSING_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.CLOSING_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.EXPECTED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.FORWARD_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.INTERIM_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.INTERIM_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.INTERIM_CLEARED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.OPENING_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.PREVIOUSLY_CLOSED_BOOKED;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalLimitType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.CreditLineEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@Slf4j
@RequiredArgsConstructor
public class DefaultCreditCardBalanceMapper implements CreditCardBalanceMapper {

    private static final List<UkObBalanceType> PREFERRED_BALANCE_TYPES =
            ImmutableList.of(
                    INTERIM_BOOKED,
                    INTERIM_CLEARED,
                    CLOSING_BOOKED,
                    PREVIOUSLY_CLOSED_BOOKED,
                    OPENING_BOOKED,
                    INTERIM_AVAILABLE,
                    CLOSING_AVAILABLE,
                    FORWARD_AVAILABLE,
                    EXPECTED);

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
                .map(entity -> getAccountAmount(entity, balances))
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

        logCreditLineTypes(balances);
        if (creditLines.isEmpty()) {
            log.debug(
                    "Calculating available credit impossible. API did not return credit lines. Setting to 0.");
            return ExactCurrencyAmount.zero(getCurrency(balances));
        } else {
            return valueExtractor
                    .pickByValuePriority(
                            creditLines,
                            CreditLineEntity::getType,
                            PREFERRED_AVAILABLE_CREDIT_LINES)
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
                                                            ',',
                                                            PREFERRED_AVAILABLE_CREDIT_LINES)));
        }
    }

    private String getCurrency(Collection<AccountBalanceEntity> balances) {
        return balances.stream()
                .map(balance -> balance.getAmount().getCurrencyCode())
                .findFirst()
                .orElseGet(
                        () -> {
                            log.warn("No currency was returned when fetching available credit");
                            return "";
                        });
    }

    private ExactCurrencyAmount getAccountAmount(
            AccountBalanceEntity entity, Collection<AccountBalanceEntity> balances) {
        log.info(
                "[CARD BALANCE] Picked {} from available {}", entity.getType(), getTypes(balances));
        return entity.getAmount();
    }

    private List<UkObBalanceType> getTypes(Collection<AccountBalanceEntity> balances) {
        return balances.stream().map(AccountBalanceEntity::getType).collect(Collectors.toList());
    }

    private void logCreditLineTypes(Collection<AccountBalanceEntity> balances) {
        log.info(
                "[CARD CREDIT LINE] Balances with credit line types {}\n",
                balances.stream()
                        .map(balance -> StringUtils.join(balance.printTypeWithCreditLines(), "\n"))
                        .collect(Collectors.toList()));
    }
}
