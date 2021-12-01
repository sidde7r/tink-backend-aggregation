package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Currency;
import java.util.Locale;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class HolderDto {

    private HolderProfileDto profile;

    private LocalizationPreferences localizationPreferences;

    @JsonIgnore
    public String getCurrencyCode() {

        /* Bank returns currency locale where it currently is not available in the locales library */
        if (AmericanExpressConstants.CurrencyLocale.EN_EU.equalsIgnoreCase(
                localizationPreferences.getCurrencyLocale())) {
            return Currency.getInstance(AmericanExpressConstants.CurrencyCode.EUR)
                    .getCurrencyCode();
        }
        return Currency.getInstance(
                        Locale.forLanguageTag(localizationPreferences.getCurrencyLocale()))
                .getCurrencyCode();
    }
}
