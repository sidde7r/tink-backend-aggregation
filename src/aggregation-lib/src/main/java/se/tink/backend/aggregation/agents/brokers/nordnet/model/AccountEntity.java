package se.tink.backend.aggregation.agents.brokers.nordnet.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Objects;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.libraries.account.AccountIdentifier;

public class AccountEntity {
    @JsonProperty("accno")
    private String accountId;
    @JsonProperty("bank_accno")
    private String accountNumber;
    @JsonProperty("account_code")
    private String accountCode;
    @JsonProperty("type")
    private String accountType;
    @JsonProperty("default")
    private boolean isMainAccount;
    @JsonProperty("alias")
    private String name;
    @JsonProperty("today")
    private double todaysMarketChange;
    @JsonProperty("own_capital")
    private BalanceEntity balance;

    public Account toAccount(AccountTypes accountType) {
        Account account = new Account();
        account.setBankId(accountId);
        account.setAccountNumber(accountNumber);
        account.putIdentifier(AccountIdentifier.create(AccountIdentifier.Type.SE, accountNumber));
        account.setType(accountType);
        account.setName(name);
        account.setBalance(balance.getAmount());

        return account;
    }

    public boolean isSwedishAccount() {
        return balance != null && Objects.equals(balance.getCurrency(), "SEK");
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public boolean isMainAccount() {
        return isMainAccount;
    }

    public void setMainAccount(boolean mainAccount) {
        isMainAccount = mainAccount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTodaysMarketChange() {
        return todaysMarketChange;
    }

    public void setTodaysMarketChange(double todaysMarketChange) {
        this.todaysMarketChange = todaysMarketChange;
    }

    public BalanceEntity getBalance() {
        return balance;
    }

    public void setBalance(BalanceEntity balance) {
        this.balance = balance;
    }

    public Portfolio toPortfolio() {
        Portfolio portfolio = new Portfolio();

        portfolio.setRawType(getAccountCode());
        portfolio.setType(getPortfolioType());
        portfolio.setTotalValue(getBalance().getAmount());
        portfolio.setUniqueIdentifier(getAccountId());

        return portfolio;
    }

    @JsonIgnore
    private boolean isTypeOccupationalPension() {
        return Strings.nullToEmpty(accountType).toLowerCase().contains("btp1");
    }

    private Portfolio.Type getPortfolioType() {
        switch (Strings.nullToEmpty(accountCode).toLowerCase()) {
        case "dep":
            return Portfolio.Type.DEPOT;
        case "isk":
            return Portfolio.Type.ISK;
        case "kf":
            return Portfolio.Type.KF;
        case "tjf":
            return Portfolio.Type.PENSION;
        default:
            return (isTypeOccupationalPension() ? Portfolio.Type.PENSION : Portfolio.Type.OTHER);
        }
    }
}
