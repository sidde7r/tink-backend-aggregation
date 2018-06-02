package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankTradingAssetPortfolio {
    private int status;
    private String portfolioId;
    private String name;
    private boolean readonly;
    private String bookentryAccount;
    private String serviceLevel;
    private boolean pensionPortfolio;
    private int portfolioCount;

    @JsonIgnore
    public boolean isValid() {
        return !Strings.isNullOrEmpty(portfolioId);
    }

    public int getStatus() {
        return status;
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public String getName() {
        return name;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public String getBookentryAccount() {
        return bookentryAccount;
    }

    public String getServiceLevel() {
        return serviceLevel;
    }

    public boolean isPensionPortfolio() {
        return pensionPortfolio;
    }

    public int getPortfolioCount() {
        return portfolioCount;
    }
}
