package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.system.rpc.Portfolio;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustodyAccountResponse extends AbstractResponse {
    private String custodyAccountNumber;
    private String iskAccountNumber;
    private String accountNumberFormatted;
    private String type;
    private String title;
    private String infoText;
    private AmountEntity marketValue;
    private String pensionSystemStr;
    private String ownerName;
    private PerformanceEntity performance;
    private AmountEntity mainDepositAccountBalance;
    private List<HoldingsListsEntity> holdingLists;

    public String getCustodyAccountNumber() {
        return custodyAccountNumber;
    }

    public void setCustodyAccountNumber(String custodyAccountNumber) {
        this.custodyAccountNumber = custodyAccountNumber;
    }

    public String getIskAccountNumber() {
        return iskAccountNumber;
    }

    public void setIskAccountNumber(String iskAccountNumber) {
        this.iskAccountNumber = iskAccountNumber;
    }

    public String getAccountNumberFormatted() {
        return accountNumberFormatted;
    }

    public void setAccountNumberFormatted(String accountNumberFormatted) {
        this.accountNumberFormatted = accountNumberFormatted;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInfoText() {
        return infoText;
    }

    public void setInfoText(String infoText) {
        this.infoText = infoText;
    }

    public AmountEntity getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(AmountEntity marketValue) {
        this.marketValue = marketValue;
    }

    public String getPensionSystemStr() {
        return pensionSystemStr;
    }

    public void setPensionSystemStr(String pensionSystemStr) {
        this.pensionSystemStr = pensionSystemStr;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public PerformanceEntity getPerformance() {
        return performance;
    }

    public void setPerformance(PerformanceEntity performance) {
        this.performance = performance;
    }

    public AmountEntity getMainDepositAccountBalance() {
        return mainDepositAccountBalance;
    }

    public void setMainDepositAccountBalance(
            AmountEntity mainDepositAccountBalance) {
        this.mainDepositAccountBalance = mainDepositAccountBalance;
    }

    public List<HoldingsListsEntity> getHoldingLists() {
        return holdingLists;
    }

    public void setHoldingLists(
            List<HoldingsListsEntity> holdingLists) {
        this.holdingLists = holdingLists;
    }

    public Account toAccount() {
        Account account = new Account();

        account.setAccountNumber(getCustodyAccountNumber());
        account.setBalance(getMarketValue().getAmount());
        account.setBankId(getUniqueIdentifier());
        account.setType(AccountTypes.INVESTMENT);
        account.setName(getTitle());

        return account;
    }

    public Portfolio toPortfolio() {
        Portfolio portfolio = new Portfolio();

        portfolio.setRawType(getType());
        portfolio.setType(getPortfolioType());
        portfolio.setTotalProfit(getPerformance() != null && getPerformance().getChangeAmount() != null ?
                getPerformance().getChangeAmount().getAmount() : null);
        portfolio.setTotalValue(getMarketValue().getAmount());
        portfolio.setUniqueIdentifier(getUniqueIdentifier());
        portfolio.setCashValue(getMainDepositAccountBalance() != null ?
                getMainDepositAccountBalance().getAmount() : null);

        return portfolio;
    }

    private String getUniqueIdentifier() {
        return Objects.equals("isk", getType().toLowerCase()) ? getIskAccountNumber() : getCustodyAccountNumber();
    }

    private Portfolio.Type getPortfolioType() {
        switch (getType().toLowerCase()) {
        case "isk":
            return Portfolio.Type.ISK;
        case "normal":
            return Portfolio.Type.DEPOT;
        default:
            return Portfolio.Type.OTHER;
        }
    }
}
