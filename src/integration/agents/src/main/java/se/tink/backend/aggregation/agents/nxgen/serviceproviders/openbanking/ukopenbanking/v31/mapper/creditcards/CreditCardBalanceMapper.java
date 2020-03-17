package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.creditcards;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountBalanceEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;

public interface CreditCardBalanceMapper {

    ExactCurrencyAmount getAccountBalance(Collection<AccountBalanceEntity> balances);

    ExactCurrencyAmount getAvailableCredit(Collection<AccountBalanceEntity> balances);
}
