package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.backend.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EquityTradersEntity {
    private String name;
    private List<OperationsEntity> operations;
    @JsonProperty("fullyFormattedNumber")
    private String accountNumber;
    private List<HoldingsEntity> holdings;
    private TotalEquitiesEntity totalEquities;
    private Boolean pawned;
    private List<SettlementEntity> settlements;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<OperationsEntity> getOperations() {
        return operations;
    }

    public void setOperations(
            List<OperationsEntity> operations) {
        this.operations = operations;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public List<HoldingsEntity> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<HoldingsEntity> holdings) {
        this.holdings = holdings;
    }

    public TotalEquitiesEntity getTotalEquities() {
        return totalEquities;
    }

    public void setTotalEquities(TotalEquitiesEntity totalEquities) {
        this.totalEquities = totalEquities;
    }

    public Boolean getPawned() {
        return pawned;
    }

    public void setPawned(Boolean pawned) {
        this.pawned = pawned;
    }

    public List<SettlementEntity> getSettlements() {
        return settlements;
    }

    public void setSettlements(
            List<SettlementEntity> settlements) {
        this.settlements = settlements;
    }

    public Optional<Account> toAccount() {
        TotalEquitiesEntity totalEquities = getTotalEquities();
        if (totalEquities == null || totalEquities.getMarketValue() == null ||
                totalEquities.getMarketValue().getAmount() == null ||
                totalEquities.getMarketValue().getAmount().isEmpty()) {
            return Optional.empty();
        }

        Account account = new Account();

        account.setAccountNumber(getAccountNumber());
        account.setBankId(getAccountNumber());
        account.setBalance(StringUtils.parseAmount(totalEquities.getMarketValue().getAmount()));
        account.setName(getName());
        account.setType(AccountTypes.INVESTMENT);

        return Optional.of(account);
    }

    public Portfolio toPortfolio() {
        Portfolio portfolio = new Portfolio();

        portfolio.setRawType(getName());
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setTotalProfit(
                totalEquities.getChangeAbsolute() != null && totalEquities.getChangeAbsolute().getAmount() != null ?
                StringUtils.parseAmount(totalEquities.getChangeAbsolute().getAmount()) : null);
        // Since this is change in toAccount and we only create a portfolio if the account is present
        // we don't have to do all checks again.
        portfolio.setTotalValue(StringUtils.parseAmount(totalEquities.getMarketValue().getAmount()));
        portfolio.setUniqueIdentifier(getAccountNumber());
        portfolio.setCashValue(totalEquities.getBuyingPower() != null ?
                StringUtils.parseAmount(totalEquities.getBuyingPower().getAmount()) : null);

        return portfolio;
    }
}
