package se.tink.backend.aggregation.credit.safe.rpc;

import java.util.List;

public class PortfolioListResponse {
    private List<String> portfolios;

    public List<String> getPortfolios() {
        return portfolios;
    }

    public void setPortfolios(List<String> portfolios) {
        this.portfolios = portfolios;
    }
}
