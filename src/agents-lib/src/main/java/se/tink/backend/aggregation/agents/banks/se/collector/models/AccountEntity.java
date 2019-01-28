package se.tink.backend.aggregation.agents.banks.se.collector.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.agents.rpc.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.agents.rpc.AccountTypes;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity {
    private String accountId;
    private String accountType;
    private double balance;
    private double interestRate;
    private double accruedInterest;
    private Date nextCapitalizationDate;
    private boolean activated;
    private DepositInfo depositInfo = new DepositInfo();
    @JsonIgnore
    private WithdrawalAccount withdrawalAccount = new WithdrawalAccount();

    public Date getNextCapitalizationDate() {
        return nextCapitalizationDate;
    }

    public void setNextCapitalizationDate(Date nextCapitalizationDate) {
        this.nextCapitalizationDate = nextCapitalizationDate;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public double getAccruedInterest() {
        return accruedInterest;
    }

    public void setAccruedInterest(double accruedInterest) {
        this.accruedInterest = accruedInterest;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public AccountTypes getAccountType() {
        String type = Strings.nullToEmpty(accountType).toLowerCase();

        if (type.contains("save") || type.contains("sparkonto")) {
            return AccountTypes.SAVINGS;
        }

        return AccountTypes.OTHER;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public DepositInfo getDepositInfo() {
        return depositInfo;
    }

    public void setDepositInfo(DepositInfo depositInfo) {
        this.depositInfo = depositInfo;
    }

    public WithdrawalAccount getWithdrawalAccount() {
        return withdrawalAccount;
    }

    @JsonProperty("bank")
    public void setBank(String bank) {
        withdrawalAccount.setBank(bank);
    }

    @JsonProperty("bankClearingNr")
    public void setClearingNumber(String clearingNumber) {
        withdrawalAccount.setClearingNumber(clearingNumber);
    }

    @JsonProperty("bankAccountNr")
    public void setAccountNumber(String accountNumber) {
        withdrawalAccount.setAccountNumber(accountNumber);
    }

    @JsonIgnore
    public AccountIdentifier getWithdrawalIdentifier() {
        return withdrawalAccount.toIdentifier();
    }

    public Account toTinkAccount() {
        Account account = new Account();
        account.setName(accountType);
        account.setType(getAccountType());
        account.setBalance(balance);
        account.setBankId(accountId);
        account.putIdentifier(depositInfo.toIdentifier());

        return account;
    }


    @JsonProperty("NextCapitalizationDate")
    public void setNextCapitalizationDateCaps(Date nextCapitalizationDate) {
        this.nextCapitalizationDate = nextCapitalizationDate;
    }


    @JsonProperty("InterestRate")
    public void setInterestRateCaps(double interestRate) {
        this.interestRate = interestRate;
    }


    @JsonProperty("AccruedInterest")
    public void setAccruedInterestCaps(double accruedInterest) {
        this.accruedInterest = accruedInterest;
    }


    @JsonProperty("AccountId")
    public void setAccountIdCaps(String accountId) {
        this.accountId = accountId;
    }

    @JsonProperty("Balance")
    public void setBalanaceCaps(double balance) {
        this.balance = balance;
    }

    @JsonProperty("AccountType")
    public void setAccountTypeCaps(String accountType) {
        this.accountType = accountType;
    }

    @JsonProperty("Activated")
    public void setActivatedCaps(boolean activated) {
        this.activated = activated;
    }

    @JsonProperty("Bank")
    public void setBankCaps(String bank) {
        this.withdrawalAccount.setBank(bank);
    }

    @JsonProperty("BankClearingNr")
    public void setClearingNumberCaps(String clearingNumber) {
        this.withdrawalAccount.setClearingNumber(clearingNumber);
    }

    @JsonProperty("BankAccountNr")
    public void setAccountNumberCaps(String accountNumber) {
        this.withdrawalAccount.setAccountNumber(accountNumber);
    }

    @JsonProperty("DepositInfo")
    public void setDepositInfoCaps(DepositInfo depositInfo) {
        this.depositInfo = depositInfo;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("accountId", accountId)
                .add("accountType", accountType)
                .add("balance", balance)
                .add("interestRate", interestRate)
                .add("accruedInterest", accruedInterest)
                .add("nextCapitalizationDate", nextCapitalizationDate)
                .add("withdrawalInfo", withdrawalAccount)
                .add("depositInfo", depositInfo)
                .toString();
    }
}
