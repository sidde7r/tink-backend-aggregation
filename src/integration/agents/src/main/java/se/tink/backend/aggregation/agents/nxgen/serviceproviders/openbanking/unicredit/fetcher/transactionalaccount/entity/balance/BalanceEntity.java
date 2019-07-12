package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.balance;

import java.util.Arrays;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity {

    public String balanceType;
    public AmountEntity balanceAmount;
    public String referenceDate;
    public String lastChangeDateTime;

    public Amount getAmount() {
        return Optional.ofNullable(balanceAmount)
                .orElseThrow(IllegalStateException::new)
                .toTinkAmount();
    }

    public int getBalanceMappingPriority() {

        return Arrays.stream(BalanceType.values())
                .filter(enumBalanceType -> enumBalanceType.getValue().equalsIgnoreCase(balanceType))
                .findAny()
                .map(BalanceType::getPriority)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                UnicreditConstants.ErrorMessages
                                                        .UNDEFINED_BALANCE_TYPE,
                                                balanceType,
                                                BalanceType.class.getName())));
    }
}
