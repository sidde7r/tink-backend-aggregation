package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExchangeRateEntity {
    private String currencyFrom;
    private double rateFrom;
    private String currencyTo;
    private double rateTo;
    private String rateDate;

    public String getCurrencyFrom() {
        return currencyFrom;
    }

    public double getRateFrom() {
        return rateFrom;
    }

    public String getCurrencyTo() {
        return currencyTo;
    }

    public double getRateTo() {
        return rateTo;
    }

    public String getRateDate() {
        return rateDate;
    }
}
