package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.transactionalaccounts;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.CLOSING_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.CLOSING_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.EXPECTED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.FORWARD_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.INTERIM_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.INTERIM_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.OPENING_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.OPENING_BOOKED;

import com.google.common.collect.ImmutableList;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
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
public class TransactionalAccountBalanceMapper {

    private static final List<AccountBalanceType> TRANSACTIONAL_ACCOUNT_BALANCE_PREFERRED_TYPES =
            ImmutableList.of(
                    INTERIM_BOOKED,
                    OPENING_BOOKED,
                    CLOSING_BOOKED,
                    EXPECTED,
                    INTERIM_AVAILABLE,
                    OPENING_AVAILABLE,
                    CLOSING_AVAILABLE);

    private static final List<AccountBalanceType> AVAILABLE_BALANCE_PREFERRED_TYPES =
            ImmutableList.of(
                    INTERIM_AVAILABLE,
                    EXPECTED,
                    FORWARD_AVAILABLE,
                    OPENING_AVAILABLE,
                    CLOSING_AVAILABLE);

    private final PrioritizedValueExtractor valueExtractor;

    public ExactCurrencyAmount getAccountBalance(Collection<AccountBalanceEntity> balances) {
        return valueExtractor
                .pickByValuePriority(
                        balances,
                        AccountBalanceEntity::getType,
                        TRANSACTIONAL_ACCOUNT_BALANCE_PREFERRED_TYPES)
                .map(AccountBalanceEntity::getAmount)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Could not extract account balance. No available balance with type of: "
                                                + StringUtils.join(
                                                        ',',
                                                        TRANSACTIONAL_ACCOUNT_BALANCE_PREFERRED_TYPES)));
    }

    public Optional<ExactCurrencyAmount> getAvailableBalance(
            Collection<AccountBalanceEntity> balances) {
        return valueExtractor
                .pickByValuePriority(
                        balances, AccountBalanceEntity::getType, AVAILABLE_BALANCE_PREFERRED_TYPES)
                .map(AccountBalanceEntity::getAmount);
    }

    public Optional<ExactCurrencyAmount> calculateAvailableCredit(
            Collection<AccountBalanceEntity> balances) {

        return balances.stream()
                .flatMap(balance -> CollectionUtils.emptyIfNull(balance.getCreditLine()).stream())
                .filter(cl -> ExternalLimitType.AVAILABLE.equals(cl.getType()))
                .findAny()
                .map(CreditLineEntity::getAmount)
                .map(a -> ExactCurrencyAmount.of(a.getUnsignedAmount(), a.getCurrency()));
    }

    public Optional<ExactCurrencyAmount> calculateCreditLimit(
            Collection<AccountBalanceEntity> balances) {
        List<CreditLineEntity> creditLines = getAllCreditLines(balances);

        Option<ExactCurrencyAmount> creditLimit =
                getLimitByType(creditLines, ExternalLimitType.CREDIT);

        return creditLimit
                .orElse(() -> sumPreAgreedAndEmergencyLimitsIfPreAgreedAvailable(creditLines))
                .toJavaOptional();
    }

    private Option<ExactCurrencyAmount> sumPreAgreedAndEmergencyLimitsIfPreAgreedAvailable(
            List<CreditLineEntity> creditLines) {

        Option<ExactCurrencyAmount> preAgreedLimit =
                getLimitByType(creditLines, ExternalLimitType.PRE_AGREED);
        Option<ExactCurrencyAmount> emergencyLimit =
                getLimitByType(creditLines, ExternalLimitType.EMERGENCY);

        return preAgreedLimit.map(
                preAgreed -> emergencyLimit.map(preAgreed::add).getOrElse(preAgreed));
    }

    private List<CreditLineEntity> getAllCreditLines(Collection<AccountBalanceEntity> balances) {
        return balances.stream()
                .flatMap(balance -> CollectionUtils.emptyIfNull(balance.getCreditLine()).stream())
                .collect(Collectors.toList());
    }

    private Option<ExactCurrencyAmount> getLimitByType(
            Collection<CreditLineEntity> creditLines, ExternalLimitType type) {

        return Stream.ofAll(creditLines)
                .find(cl -> type.equals(cl.getType()))
                .map(CreditLineEntity::getAmount)
                .map(a -> ExactCurrencyAmount.of(a.getUnsignedAmount(), a.getCurrency()));
    }
}
