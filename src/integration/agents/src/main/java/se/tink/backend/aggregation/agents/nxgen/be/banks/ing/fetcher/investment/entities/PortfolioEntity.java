package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.investment.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PortfolioEntity {

    private String portfolioAccountName;
    private String portfolioAccountType;
    private String portfolioBalance;
    private String portfolioAccountNumberBBAN;
    private List<SecurityEntity> securities;

    public String getPortfolioAccountName() {
        return portfolioAccountName;
    }

    public String getPortfolioAccountType() {
        return portfolioAccountType;
    }

    public String getPortfolioBalance() {
        return portfolioBalance;
    }

    public String getPortfolioAccountNumberBBAN() {
        return portfolioAccountNumberBBAN;
    }

    public List<SecurityEntity> getSecurities() {
        return securities;
    }
}
