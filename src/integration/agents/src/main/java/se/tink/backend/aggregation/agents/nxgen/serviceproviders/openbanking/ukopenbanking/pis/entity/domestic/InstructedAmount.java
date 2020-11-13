package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.domestic;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class InstructedAmount {
    private String amount;
    private String currency;

    // Used in serialization unit tests
    protected InstructedAmount() {}

    public InstructedAmount(ExactCurrencyAmount amount) {
        this.amount = amount.getStringValue(getValueFormat());
        this.currency = amount.getCurrencyCode();
    }

    static DecimalFormat getValueFormat() {
        DecimalFormat format = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.UK);
        DecimalFormatSymbols decimalFormatSymbols = format.getDecimalFormatSymbols();
        decimalFormatSymbols.setCurrencySymbol("");
        format.setDecimalFormatSymbols(decimalFormatSymbols);
        format.setGroupingUsed(false);

        return format;
    }

    public ExactCurrencyAmount toTinkAmount() {
        return new ExactCurrencyAmount(new BigDecimal(amount), currency);
    }

    String getAmount() {
        return amount;
    }
}
