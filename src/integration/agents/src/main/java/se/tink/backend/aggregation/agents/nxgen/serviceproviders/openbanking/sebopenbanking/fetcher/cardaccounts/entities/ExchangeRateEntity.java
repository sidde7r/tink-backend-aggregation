package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExchangeRateEntity {

    private String currencyFrom;
    private String currencyTo;
    private BigDecimal rate;
    private String rateDate;

    public String getCurrencyFrom() {
        return currencyFrom;
    }

    public String getCurrencyTo() {
        return currencyTo;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public String getRateDate() {
        return rateDate;
    }
}
