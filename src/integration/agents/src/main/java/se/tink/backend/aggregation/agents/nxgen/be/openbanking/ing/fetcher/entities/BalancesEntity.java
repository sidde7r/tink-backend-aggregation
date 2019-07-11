package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Arrays;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalancesEntity {

    private String balanceType;
    private BalanceAmountEntity balanceAmount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date referenceDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date lastChangeDateTime;

    public int getBalanceMappingPriority() {

        return Arrays.stream(BalanceType.values())
                .filter(enumBalanceType -> enumBalanceType.getValue().equalsIgnoreCase(balanceType))
                .findAny()
                .map(BalanceType::getPriority)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                IngConstants.ErrorMessages.UNDEFINED_BALANCE_TYPE,
                                                balanceType,
                                                BalanceType.class.getName())));
    }

    public String getCurrency() {
        return balanceAmount.getCurrency();
    }

    public Amount getAmount() {
        return balanceAmount.toAmount();
    }
}
