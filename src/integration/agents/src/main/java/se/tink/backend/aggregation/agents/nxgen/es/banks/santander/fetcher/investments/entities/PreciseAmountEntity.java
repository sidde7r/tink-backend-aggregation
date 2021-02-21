package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
@XmlRootElement
public class PreciseAmountEntity {
    @JsonProperty("NUMERO_IMPORTE_PRECISO")
    private String amount;

    @JsonProperty("DIVISA")
    private String currency;

    public String getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    @JsonIgnore
    public ExactCurrencyAmount getTinkAmount() {
        if (!Strings.isNullOrEmpty(currency)) {
            return ExactCurrencyAmount.of(getValueAsDouble(), currency);
        }

        return ExactCurrencyAmount.inEUR(getValueAsDouble());
    }

    @JsonIgnore
    private double getValueAsDouble() {
        return Strings.isNullOrEmpty(amount) ? 0d : StringUtils.parseAmount(amount);
    }
}
