package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityHoldingsResponse {
    @JsonIgnore
    private static final String NAME = "Aktiedepå";
    private SecurityHoldingsEntity securityHoldings;

    public SecurityHoldingsEntity getSecurityHoldings() {
        return securityHoldings;
    }

    public void setSecurityHoldings(
            SecurityHoldingsEntity securityHoldings) {
        this.securityHoldings = securityHoldings;
    }

    public Optional<Account> toShareDepotAccount(ShareDepotWrapper shareDepotWrapper) {
        if (getSecurityHoldings() == null || getSecurityHoldings().getTotalMarketValue() == null
                || shareDepotWrapper == null || shareDepotWrapper.getDepot() == null
                || shareDepotWrapper.getDepot().getDepotNumber() == null) {
            return Optional.empty();
        }

        Account account = new Account();

        account.setAccountNumber(shareDepotWrapper.getDepot().getDepotNumber());
        account.setBankId(shareDepotWrapper.getDepot().getDepotNumber());
        account.setBalance(StringUtils.parseAmount(getSecurityHoldings().getTotalMarketValue()));
        account.setName(NAME); // LF don't provide a name field
        account.setType(AccountTypes.INVESTMENT);

        return Optional.of(account);
    }

    public Portfolio toShareDepotPortfolio(ShareDepotWrapper shareDepotWrapper, Double cashValue) {
        Portfolio portfolio = new Portfolio();

        double totalMarketValue = StringUtils.parseAmount(getSecurityHoldings().getTotalMarketValue());

        portfolio.setCashValue(cashValue);
        portfolio.setTotalProfit(getSecurityHoldings().getTotalAcquisitionCost() != null ?
                totalMarketValue - StringUtils.parseAmount(getSecurityHoldings().getTotalAcquisitionCost()) : null);
        portfolio.setTotalValue(totalMarketValue);
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setUniqueIdentifier(shareDepotWrapper.getDepot().getDepotNumber());

        return portfolio;
    }
}
