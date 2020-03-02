package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountBalanceEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;

public interface CreditCardBalanceMapper {

    AccountBalanceEntity getAccountBalance(Collection<AccountBalanceEntity> balances);

    ExactCurrencyAmount getAvailableCredit(Collection<AccountBalanceEntity> balances);
}
