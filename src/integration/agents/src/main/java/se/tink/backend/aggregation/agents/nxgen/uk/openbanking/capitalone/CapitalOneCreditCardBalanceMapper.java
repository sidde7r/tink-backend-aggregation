package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.capitalone;

import java.util.Collection;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.DefaultCreditCardBalanceMapper;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RequiredArgsConstructor
public class CapitalOneCreditCardBalanceMapper implements CreditCardBalanceMapper {

    private final DefaultCreditCardBalanceMapper defaultCreditCardBalanceMapper;

    @Override
    public ExactCurrencyAmount getAccountBalance(Collection<AccountBalanceEntity> balances) {
        return balances.stream()
                .map(AccountBalanceEntity::getAmount)
                .findFirst() // There is always only one balance per credit card account
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Could not extract credit card account balance. No available balance with type of: "
                                                + UkOpenBankingApiDefinitions.AccountBalanceType
                                                        .OPENING_BOOKED));
    }

    @Override
    public ExactCurrencyAmount getAvailableCredit(Collection<AccountBalanceEntity> balances) {
        return defaultCreditCardBalanceMapper.getAvailableCredit(balances);
    }
}
