package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonInclude(Include.NON_NULL)
@JsonObject
public class SibsAmountEntity {

    @JsonIgnore private static final String AMOUNT_PATTERN = "0.00";
    @JsonIgnore private static final char DECIMAL_SEPARATOR = '.';

    private String currency;
    private String content;

    public static SibsAmountEntity of(ExactCurrencyAmount exactCurrencyAmount) {
        SibsAmountEntity sa = new SibsAmountEntity();
        sa.setCurrency(exactCurrencyAmount.getCurrencyCode());

        DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();
        decimalSymbols.setDecimalSeparator(DECIMAL_SEPARATOR);
        String value =
                new DecimalFormat(AMOUNT_PATTERN, decimalSymbols)
                        .format(exactCurrencyAmount.getDoubleValue());

        sa.setContent(value);
        return sa;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ExactCurrencyAmount toTinkAmount() {
        return new ExactCurrencyAmount(new BigDecimal(content), currency);
    }
}
