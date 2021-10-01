package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.capitalone;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.DefaultCreditCardBalanceMapper;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@RequiredArgsConstructor
public class CapitalOneCreditCardBalanceMapper implements CreditCardBalanceMapper {

    private final DefaultCreditCardBalanceMapper defaultCreditCardBalanceMapper;

    @Override
    public ExactCurrencyAmount getAccountBalance(Collection<AccountBalanceEntity> balances) {
        return balances.stream()
                .map(entity -> getAccountAmount(entity, balances))
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

    private ExactCurrencyAmount getAccountAmount(
            AccountBalanceEntity entity, Collection<AccountBalanceEntity> balances) {
        log.info(
                "[CARD BALANCE] Picked {} from available {}", entity.getType(), getTypes(balances));
        return entity.getAmount();
    }

    private List<AccountBalanceType> getTypes(Collection<AccountBalanceEntity> balances) {
        return balances.stream().map(AccountBalanceEntity::getType).collect(Collectors.toList());
    }
}
