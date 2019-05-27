package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExchangeRate {

    private String currencyFrom;

    private String currencyTo;

    private Double rate;

    private String rateDate;

    public String getCurrencyFrom() {
        return currencyFrom;
    }

    public String getCurrencyTo() {
        return currencyTo;
    }

    public Double getRate() {
        return rate;
    }

    public String getRateDate() {
        return rateDate;
    }
}
