package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.nationwide;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.CLOSING_AVAILABLE;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.CreditLineEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.creditcards.CreditCardBalanceMapper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.amount.ExactCurrencyAmount;

class NationwideCreditCardBalanceMapper implements CreditCardBalanceMapper {

    @Override
    public ExactCurrencyAmount getAccountBalance(Collection<AccountBalanceEntity> balances) {
        return balances.stream()
                .filter(b -> CLOSING_AVAILABLE.equals(b.getType()))
                .map(AccountBalanceEntity::getAmount)
                .findAny()
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Unable to get available credit - no CLOSING_AVAILABLE available balance found."));
    }

    @Override
    public ExactCurrencyAmount getAvailableCredit(Collection<AccountBalanceEntity> balances) {
        List<CreditLineEntity> availableCreditLines =
                balances.stream()
                        .flatMap(
                                balance ->
                                        CollectionUtils.emptyIfNull(balance.getCreditLine())
                                                .stream())
                        .collect(Collectors.toList());

        if (availableCreditLines.isEmpty()) {
            // for now API does not return any credit line, so its impossible to calculate available
            // credit
            return ExactCurrencyAmount.of(BigDecimal.ZERO, "GBP");
        } else {
            throw new NotImplementedException("Calculation of available credit not implemented.");
        }
    }
}
