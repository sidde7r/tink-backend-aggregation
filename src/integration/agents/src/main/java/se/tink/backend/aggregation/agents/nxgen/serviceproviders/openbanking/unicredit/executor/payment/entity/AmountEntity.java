package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity {

    private String amount;
    private String currency;

    public AmountEntity() {}

    public AmountEntity(String amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    @JsonIgnore
    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(StringUtils.parseAmount(amount), currency);
    }
}
