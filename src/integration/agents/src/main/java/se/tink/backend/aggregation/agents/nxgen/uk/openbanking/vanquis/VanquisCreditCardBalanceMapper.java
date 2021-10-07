package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.vanquis;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.CLOSING_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.CLOSING_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.FORWARD_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.INTERIM_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.INTERIM_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.OPENING_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.PREVIOUSLY_CLOSED_BOOKED;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardBalanceMapper;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@Slf4j
@RequiredArgsConstructor
public class VanquisCreditCardBalanceMapper implements CreditCardBalanceMapper {

    private static final List<AccountBalanceType> PREFERRED_BALANCE_TYPES =
            ImmutableList.of(
                    INTERIM_BOOKED,
                    CLOSING_BOOKED,
                    PREVIOUSLY_CLOSED_BOOKED,
                    OPENING_BOOKED,
                    INTERIM_AVAILABLE,
                    CLOSING_AVAILABLE,
                    FORWARD_AVAILABLE);

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
        logCreditLineTypes(balances);
        return balances.stream()
                .filter(b -> FORWARD_AVAILABLE.equals(b.getType()))
                .findAny()
                .map(entity -> getAvailableAmount(entity, balances))
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Unable to get available credit - no FORWARD AVAILABLE balance found."));
    }

    private ExactCurrencyAmount getAccountAmount(
            AccountBalanceEntity entity, Collection<AccountBalanceEntity> balances) {
        log.info(
                "[CARD BALANCE] Picked {} from available {}", entity.getType(), getTypes(balances));
        return entity.getAmount();
    }

    private ExactCurrencyAmount getAvailableAmount(
            AccountBalanceEntity entity, Collection<AccountBalanceEntity> balances) {
        log.info(
                "[CARD AVAILABLE BALANCE] Picked {} from available {}",
                entity.getType(),
                getTypes(balances));
        return entity.getAmount();
    }

    private List<AccountBalanceType> getTypes(Collection<AccountBalanceEntity> balances) {
        return balances.stream().map(AccountBalanceEntity::getType).collect(Collectors.toList());
    }

    private void logCreditLineTypes(Collection<AccountBalanceEntity> balances) {
        log.info(
                "[CARD CREDIT LINE] Available types {}",
                balances.stream()
                        .flatMap(
                                balance ->
                                        CollectionUtils.emptyIfNull(balance.getCreditLine())
                                                .stream())
                        .map(
                                line ->
                                        StringUtils.join(
                                                "{",
                                                line.getType(),
                                                ", included: ",
                                                line.getIncluded(),
                                                "}"))
                        .collect(Collectors.toList()));
    }
}
