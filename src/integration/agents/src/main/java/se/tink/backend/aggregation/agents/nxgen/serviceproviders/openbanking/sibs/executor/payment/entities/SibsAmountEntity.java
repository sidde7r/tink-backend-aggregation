package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonInclude(Include.NON_NULL)
@JsonObject
public class SibsAmountEntity {

    @JsonIgnore private static String AMOUNT_PATTERN = "0.00";

    private String currency;
    private String content;

    public static SibsAmountEntity of(Amount amount) {
        SibsAmountEntity sa = new SibsAmountEntity();
        sa.setCurrency(amount.getCurrency());

        DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();
        decimalSymbols.setDecimalSeparator('.');
        String value =
                new DecimalFormat(AMOUNT_PATTERN, decimalSymbols).format(amount.doubleValue());

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

    public Amount toTinkAmount() {
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setValue(new Double(content));
        return amount;
    }
}
