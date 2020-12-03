package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class InstructedAmount {

    private String amount;
    private String currency;

    public InstructedAmount(ExactCurrencyAmount amount) {
        this.amount = amount.getStringValue(getValueFormat());
        this.currency = amount.getCurrencyCode();
    }

    private static DecimalFormat getValueFormat() {
        DecimalFormat format = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.UK);
        DecimalFormatSymbols decimalFormatSymbols = format.getDecimalFormatSymbols();
        decimalFormatSymbols.setCurrencySymbol("");
        format.setDecimalFormatSymbols(decimalFormatSymbols);
        format.setGroupingUsed(false);

        return format;
    }
}
