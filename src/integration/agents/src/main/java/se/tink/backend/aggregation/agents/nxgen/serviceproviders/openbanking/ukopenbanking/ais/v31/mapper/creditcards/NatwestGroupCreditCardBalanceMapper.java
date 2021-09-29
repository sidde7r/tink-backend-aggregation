package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.EXPECTED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType.FORWARD_AVAILABLE;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.AccountBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.CreditLineEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
public class NatwestGroupCreditCardBalanceMapper implements CreditCardBalanceMapper {

    @Override
    public ExactCurrencyAmount getAccountBalance(Collection<AccountBalanceEntity> balances) {
        return balances.stream()
                .filter(b -> EXPECTED.equals(b.getType()))
                .map(entity -> getAccountAmount(entity, balances))
                .findAny()
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Unable to get account balance - no EXPECTED balance found."));
    }

    @Override
    public ExactCurrencyAmount getAvailableCredit(Collection<AccountBalanceEntity> balances) {
        logCreditLineTypes(balances);
        return balances.stream()
                .filter(b -> FORWARD_AVAILABLE.equals(b.getType()))
                .findAny()
                .map(entity -> getAvailableAmount(entity, balances))
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Unable to get available credit - no FORWARD AVAILABLE balance found."));
    }

    private ExactCurrencyAmount getAccountAmount(
            AccountBalanceEntity entity, Collection<AccountBalanceEntity> balances) {
        log.info(
                "[CARD BALANCE] Picked {} from available {}", entity.getType(), getTypes(balances));
        return entity.getAmount();
    }

    private ExactCurrencyAmount getAvailableAmount(
            AccountBalanceEntity entity, Collection<AccountBalanceEntity> balances) {
        log.info(
                "[CARD AVAILABLE BALANCE] Picked {} from available {}",
                entity.getType(),
                getTypes(balances));
        return entity.getAmount();
    }

    private List<AccountBalanceType> getTypes(Collection<AccountBalanceEntity> balances) {
        return balances.stream().map(AccountBalanceEntity::getType).collect(Collectors.toList());
    }

    private void logCreditLineTypes(Collection<AccountBalanceEntity> balances) {
        log.info(
                "[CARD CREDIT LINE] Available types {}",
                balances.stream()
                        .flatMap(
                                balance ->
                                        CollectionUtils.emptyIfNull(balance.getCreditLine())
                                                .stream())
                        .map(CreditLineEntity::getType)
                        .collect(Collectors.toList()));
    }
}
