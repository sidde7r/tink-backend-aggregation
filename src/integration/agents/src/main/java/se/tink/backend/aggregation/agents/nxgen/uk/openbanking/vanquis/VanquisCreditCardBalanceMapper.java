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
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardBalanceMapper;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

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
                .map(AccountBalanceEntity::getAmount)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Could not extract credit card account balance. No available balance with type of: "
                                                + StringUtils.join(',', PREFERRED_BALANCE_TYPES)));
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
                                        "Unable to get available credit - no FORWARD AVAILABLE balance found."));
    }
}
