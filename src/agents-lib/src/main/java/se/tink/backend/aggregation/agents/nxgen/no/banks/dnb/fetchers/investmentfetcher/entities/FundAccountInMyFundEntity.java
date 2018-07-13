package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundAccountInMyFundEntity {
    private String accountName;
    private String accountNumber;
    private double sum;
    private boolean ips;

    public String getAccountName() {
        return accountName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getSum() {
        return sum;
    }

    public boolean isIps() {
        return ips;
    }
}
