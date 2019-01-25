package se.tink.backend.aggregation.agents.banks.nordea.v15.model.savings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import java.util.List;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.investments.HoldingsEntity;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustodyAccount {

    private static Pattern ALLOWED_BANKID_PATTERN = Pattern
            .compile(Joiner.on("|").join(
                    "[\\w]+:[0-9]{11}", // ASDF:12345678901
                    "[\\w]+:[0-9]{19}", // ASDF:1234567890123456789
                    "[\\w]+:[0-9]{12}", // ASDF:123456789012
                    "[\\w]+:[0-9]{6,}\\.[0-9]", // ASDF:123456.1, ASDF:1234567890.1
                    "[\\w]+:[0-9]{14}")); // ASDF:12345678901234

    @JsonProperty("custodyAccountId")
    private String accountId;
    @JsonProperty("displayName")
    private String name;
    @JsonProperty("displayAccountNumber")
    private String accountNumber;
    @JsonProperty("baseCurrency")
    private String currency;
    private String classification;
    @JsonProperty("profitLoss")
    private String profit;
    private String marketValue;
    private List<HoldingsEntity> holdings;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getMarketValue() {
        return marketValue == null || marketValue.isEmpty() ? null : StringUtils.parseAmount(marketValue);
    }

    public void setMarketValue(String marketValue) {
        this.marketValue = marketValue;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public Double getProfit() {
        return profit == null || profit.isEmpty() ? null : StringUtils.parseAmount(profit);
    }

    public void setProfit(String profit) {
        this.profit = profit;
    }

    public Account toAccount() {
        Account account = new Account();

        account.setName(getName());
        account.setBalance(getMarketValue());
        account.setAccountNumber(getAccountNumber());
        account.setBankId(getAccountId());
        account.setType(AccountTypes.INVESTMENT);

        return account;
    }

    public Portfolio toPortfolio(Double cashValue) {
        Portfolio portfolio = new Portfolio();

        portfolio.setTotalProfit(getProfit());
        portfolio.setCashValue(cashValue);
        portfolio.setType(getPortfolioType());
        portfolio.setRawType(getName());
        portfolio.setTotalProfit(getProfit());
        portfolio.setTotalValue(getMarketValue());
        portfolio.setUniqueIdentifier(getAccountId());

        return portfolio;
    }

    public boolean hasValidBankId() {
        String bankId = getAccountId();

        // Example formats of custody accounts are FONDA:01409805511 and ASBS:270111.1
        return bankId != null && ALLOWED_BANKID_PATTERN.matcher(bankId).matches();
    }

    public List<HoldingsEntity> getHoldings() {
        return holdings;
    }

    public void setHoldings(
            List<HoldingsEntity> holdings) {
        this.holdings = holdings;
    }

    private Portfolio.Type getPortfolioType() {
        String[] accountIdArray = getAccountId().split(":");
        if (accountIdArray.length != 2) {
            throw new IllegalStateException("This should not happen since we've check the bank id pattern");
        }

        switch (accountIdArray[0].toLowerCase()) {
        case "fonda":
            return Portfolio.Type.DEPOT;
        case "isk":
            return Portfolio.Type.ISK;
        case "ips":
            return Portfolio.Type.PENSION;
        case "asbs":
            return Portfolio.Type.DEPOT;
        case "aktiv":
            // Intentional fall through
        default:
            return Portfolio.Type.OTHER;
        }
    }
}
