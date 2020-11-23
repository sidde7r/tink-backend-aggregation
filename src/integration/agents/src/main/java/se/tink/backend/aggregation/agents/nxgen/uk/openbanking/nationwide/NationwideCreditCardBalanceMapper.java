package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.nationwide;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.CreditLineEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.DefaultCreditCardBalanceMapper;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@RequiredArgsConstructor
class NationwideCreditCardBalanceMapper implements CreditCardBalanceMapper {

    private final DefaultCreditCardBalanceMapper defaultCreditCardBalanceMapper;

    @Override
    public ExactCurrencyAmount getAccountBalance(Collection<AccountBalanceEntity> balances) {
        return defaultCreditCardBalanceMapper.getAccountBalance(balances);
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
            log.debug(
                    "Calculating available credit impossible. API did not return credit lines. Setting to 0.");
            return ExactCurrencyAmount.zero("GBP");
        } else {
            return defaultCreditCardBalanceMapper.getAvailableCredit(balances);
        }
    }
}
