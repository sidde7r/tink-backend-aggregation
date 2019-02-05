package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DividendHistoryEntity {
    private String exDate;
    private double balance;
    private double rate;
    private String catype;
    private double dividend;
    private String recordDate;
    private String payDate;

    public String getExDate() {
        return exDate;
    }

    public double getBalance() {
        return balance;
    }

    public double getRate() {
        return rate;
    }

    public String getCatype() {
        return catype;
    }

    public double getDividend() {
        return dividend;
    }

    public String getRecordDate() {
        return recordDate;
    }

    public String getPayDate() {
        return payDate;
    }
}
