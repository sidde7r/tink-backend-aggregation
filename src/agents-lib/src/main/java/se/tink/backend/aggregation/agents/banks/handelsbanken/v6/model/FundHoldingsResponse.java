package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.system.rpc.Portfolio;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundHoldingsResponse extends AbstractResponse {
    private UserFundHoldingsEntity userFundHoldings;

    public UserFundHoldingsEntity getUserFundHoldings() {
        return userFundHoldings;
    }

    public void setUserFundHoldings(UserFundHoldingsEntity userFundHoldings) {
        this.userFundHoldings = userFundHoldings;
    }

    public Account createFundSummaryAccount(CustodyAccountEntity custodyAccount) {
        Account account = new Account();

        PartEntity accountDetails = getUserFundHoldings().getPart();

        account.setAccountNumber(accountDetails.getIdentifier());
        account.setBankId(accountDetails.getIdentifier());
        account.setBalance(getUserFundHoldings().getFundHoldingSummary().getMarketValue());
        account.setName(custodyAccount.getTitle());
        account.setType(AccountTypes.INVESTMENT);

        return account;
    }

    public Portfolio createFundPortfolio(CustodyAccountEntity custodyAccount) {
        Portfolio portfolio = new Portfolio();

        FundHoldingSummaryEntity fundHoldingSummary = getUserFundHoldings().getFundHoldingSummary();
        PartEntity accountDetails = getUserFundHoldings().getPart();

        portfolio.setTotalValue(fundHoldingSummary.getMarketValue());
        portfolio.setTotalProfit(fundHoldingSummary.getMarketValue() - fundHoldingSummary.getPurchaseValue());
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setRawType(custodyAccount.getType());
        portfolio.setUniqueIdentifier(accountDetails.getIdentifier());

        return portfolio;
    }
}
