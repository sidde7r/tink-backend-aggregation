package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.rpc.OpBankResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankPortfoliosEntity extends OpBankResponseEntity {
    private boolean readonly;
    private int portfolioCount;
    private String portfolioId;
    private String name;
    private String bookentryAccount;
    private String serviceLevel;
    private boolean pensionPortfolio;

    @JsonIgnore
    public boolean isValid() {
        return !Strings.isNullOrEmpty(portfolioId);
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
