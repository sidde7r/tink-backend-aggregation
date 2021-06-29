package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities;

import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@Getter
@JsonObject
public class AmountEntity {

    private String currency;
    private String amount;

    public static AmountEntity withAmount(ExactCurrencyAmount exactCurrencyAmount) {
        AmountEntity entity = new AmountEntity();
        entity.currency = exactCurrencyAmount.getCurrencyCode();
        entity.amount = toRedsysAmount(exactCurrencyAmount);
        return entity;
    }

    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(
                BigDecimal.valueOf(StringUtils.parseAmount(amount)), currency);
    }

    private static String toRedsysAmount(ExactCurrencyAmount exactCurrencyAmount) {
        return exactCurrencyAmount.getExactValue().setScale(2).toPlainString();
    }
}
