package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
@XmlRootElement
public class AmountEntity {
    @JsonProperty("IMPORTE")
    private String amount;

    @JsonProperty("DIVISA")
    private String currency;

    public String getCurrency() {
        return currency;
    }

    @JsonIgnore
    public ExactCurrencyAmount getTinkAmount() {
        return ExactCurrencyAmount.of(getAmount(), getCurrencyValue());
    }

    @JsonIgnore
    private String getCurrencyValue() {
        return Strings.isNullOrEmpty(currency) ? SantanderEsConstants.DEFAULT_CURRENCY : currency;
    }

    @JsonIgnore
    public String getAmount() {
        return Strings.isNullOrEmpty(amount) ? SantanderEsConstants.DEFAULT_INVESTMENT_AMOUNT : amount;
    }

    @JsonIgnore
    public double getAmountAsDouble() {
        return StringUtils.parseAmount(getAmount());
    }
}
