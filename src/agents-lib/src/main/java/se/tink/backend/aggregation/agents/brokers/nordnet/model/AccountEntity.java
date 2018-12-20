package se.tink.backend.aggregation.agents.brokers.nordnet.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class AccountEntity {

    @JsonProperty("accno")
    private String accountNumber;
    @JsonProperty("bank_account")
    private BankAccountEntity bankAccount;
    @JsonProperty("accid")
    private String accountId;
    @JsonProperty("bank_accno")
    private String bankAccountNumber;
    private String type;
    private String symbol;
    @JsonProperty("account_code")
    private String accountCode;
    private String role;
    @JsonProperty("is_blocked")
    private boolean blocked;
    @JsonProperty("blocked_reason")
    private String blockedReason;

    @JsonProperty("default")
    private boolean defaultAccount;
    private String alias;

    @JsonIgnore
    private AccountInfoEntity info;

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setInfo(AccountInfoEntity info) {
        this.info = info;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public String getBlockedReason() {
        return blockedReason;
    }

    /**
     * The amount of cash in the account minus what is borrowed, but those numbers are not directly available.
     * Had to guess a bit here. The exact meaning of 'pawn value', 'loan limit', 'collateral', 'account credit'
     * and others is not known.
     */
    public double getCashBalance() {

        // Essentially 'available cash' + 'remaining credit'
        double tradingPower = info.getTradingPower();

        // Approwed/full credit (Possible alternative here is the 'loan limit')
        double pawnValue = info.getPawnValue();

        // This should be: 'available cash' - 'actually utilised credit'
        double balance = tradingPower - pawnValue;

        // If this account/portfolio is used as collateral for some loan, and the market value of the investments don't
        // cover the amount, then the trading power has been decreased by the remaining part.
        // (Not 100% sure about the logic here.)
        double cashCollateral = Math.max(info.getCollateral() - info.getFullMarketvalue(), 0.0);

        return balance + cashCollateral;
    }

    public double getMarketValue() {
        return info.getFullMarketvalue();
    }

    public String getName() {
        return alias;
    }

    public Account toAccount(AccountTypes accountType) {

        Account account = new Account();

        account.setBankId(this.getAccountNumber());
        account.setAccountNumber(this.getBankAccountNumber());
        account.putIdentifier(AccountIdentifier.create(AccountIdentifier.Type.SE, this.getBankAccountNumber()));
        account.setType(accountType);
        account.setName(this.getName());
        account.setBalance(this.getCashBalance() + this.getMarketValue());

        return account;
    }

    public Portfolio toPortfolio() {

        Portfolio portfolio = new Portfolio();

        portfolio.setRawType(this.getAccountCode());
        portfolio.setType(this.getPortfolioType());
        portfolio.setTotalValue(this.getMarketValue());
        portfolio.setUniqueIdentifier(this.getAccountNumber());
        portfolio.setCashValue(this.getCashBalance());

        return portfolio;
    }

    private Portfolio.Type getPortfolioType() {
        switch (Strings.nullToEmpty(getAccountCode()).toLowerCase()) {
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

    @JsonIgnore
    private boolean isTypeOccupationalPension() {
        return Strings.nullToEmpty(type).toLowerCase().contains("btp1");
    }
}
