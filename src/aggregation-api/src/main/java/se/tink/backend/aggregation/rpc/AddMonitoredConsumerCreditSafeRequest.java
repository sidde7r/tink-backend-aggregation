package se.tink.backend.aggregation.rpc;

public class AddMonitoredConsumerCreditSafeRequest {
    private String portfolio;
    private String pnr;

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public String getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(String portfolio) {
        this.portfolio = portfolio;
    }
}
