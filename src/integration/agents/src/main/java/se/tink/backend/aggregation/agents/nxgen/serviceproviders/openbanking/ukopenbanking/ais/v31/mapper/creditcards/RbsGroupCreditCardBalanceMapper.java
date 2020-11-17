package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.EXPECTED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.FORWARD_AVAILABLE;

import java.util.Collection;
import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class RbsGroupCreditCardBalanceMapper implements CreditCardBalanceMapper {

    @Override
    public ExactCurrencyAmount getAccountBalance(Collection<AccountBalanceEntity> balances) {
        return balances.stream()
                .filter(b -> EXPECTED.equals(b.getType()))
                .map(AccountBalanceEntity::getAmount)
                .findAny()
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Unable to get account balance - no EXPECTED balance found."));
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
