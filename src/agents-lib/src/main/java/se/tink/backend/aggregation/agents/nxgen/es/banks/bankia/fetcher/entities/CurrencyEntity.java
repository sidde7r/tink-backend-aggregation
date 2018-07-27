package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CurrencyEntity {

    private String badge;
    private String shortName;
    private String control;

    @JsonProperty("divisa")
    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getShortName() {
        return shortName;
    }

    @JsonProperty("nombreCorto")
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @JsonProperty("digitoControl")
    public void setDigitoControl(String digitoControl) {
        this.control = digitoControl;
    }

    @JsonProperty("digitoControlDivisa")
    public void setDigitControlCurrency(String digitControlCurrency) {
        this.control = digitControlCurrency;
    }

}
