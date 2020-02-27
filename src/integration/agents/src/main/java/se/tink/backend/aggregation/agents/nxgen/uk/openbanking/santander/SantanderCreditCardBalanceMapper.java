package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.santander;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.FORWARD_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.OPENING_CLEARED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.PREVIOUSLY_CLOSED_BOOKED;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.CreditCardBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.PrioritizedValueExtractor;
import se.tink.libraries.amount.ExactCurrencyAmount;

class SantanderCreditCardBalanceMapper implements CreditCardBalanceMapper {

    private final PrioritizedValueExtractor valueExtractor;

    SantanderCreditCardBalanceMapper(PrioritizedValueExtractor valueExtractor) {
        this.valueExtractor = valueExtractor;
    }

    @Override
    public AccountBalanceEntity getAccountBalance(Collection<AccountBalanceEntity> balances) {
        return valueExtractor.pickByValuePriority(
                balances,
                AccountBalanceEntity::getType,
                ImmutableList.of(PREVIOUSLY_CLOSED_BOOKED, OPENING_CLEARED));
    }

    @Override
    public ExactCurrencyAmount getAvailableCredit(Collection<AccountBalanceEntity> balances) {
        return balances.stream()
                .filter(b -> b.getType().equals(FORWARD_AVAILABLE))
                .findAny()
                .map(AccountBalanceEntity::getAsCurrencyAmount)
                .orElseThrow(NoSuchElementException::new);
    }
}
