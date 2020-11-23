package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.santander;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.FORWARD_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.OPENING_CLEARED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.PREVIOUSLY_CLOSED_BOOKED;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardBalanceMapper;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@RequiredArgsConstructor
class SantanderCreditCardBalanceMapper implements CreditCardBalanceMapper {

    private static final ImmutableList<AccountBalanceType> ALLOWED_BALANCE_TYPES =
            ImmutableList.of(PREVIOUSLY_CLOSED_BOOKED, OPENING_CLEARED);

    private final PrioritizedValueExtractor valueExtractor;

    @Override
    public ExactCurrencyAmount getAccountBalance(Collection<AccountBalanceEntity> balances) {
        return valueExtractor
                .pickByValuePriority(balances, AccountBalanceEntity::getType, ALLOWED_BALANCE_TYPES)
                .map(AccountBalanceEntity::getAmount)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Could not extract credit card account balance. No available balance with type of: "
                                                + StringUtils.join(',', ALLOWED_BALANCE_TYPES)));
    }

    @Override
    public ExactCurrencyAmount getAvailableCredit(Collection<AccountBalanceEntity> balances) {
        return balances.stream()
                .filter(b -> FORWARD_AVAILABLE.equals(b.getType()))
                .findAny()
                .map(AccountBalanceEntity::getAmount)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Unable to get available credit - no forward available balance found."));
    }
}
