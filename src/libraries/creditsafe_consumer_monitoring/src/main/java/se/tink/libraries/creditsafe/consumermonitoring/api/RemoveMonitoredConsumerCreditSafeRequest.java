package se.tink.libraries.creditsafe.consumermonitoring.api;

import java.util.List;

public class RemoveMonitoredConsumerCreditSafeRequest {
    private String pnr;
    private List<String> portfolios;

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public List<String> getPortfolios() {
        return portfolios;
    }

    public void setPortfolios(List<String> portfolios) {
        this.portfolios = portfolios;
    }
}
