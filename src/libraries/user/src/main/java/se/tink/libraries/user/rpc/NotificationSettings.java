package se.tink.libraries.user.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@SuppressWarnings("serial")
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationSettings implements Serializable {

    @Tag(1)
    protected boolean balance = true;

    @Tag(2)
    protected boolean budget = true;

    @Tag(3)
    protected boolean doubleCharge = true;

    @Tag(4)
    protected boolean income = true;

    @Tag(5)
    protected boolean largeExpense = true;

    @Tag(6)
    protected boolean summaryMonthly = true;

    @Tag(7)
    protected boolean summaryWeekly = true;

    @Tag(8)
    protected boolean transaction = true;

    @Tag(9)
    protected boolean unusualAccount = true;

    @Tag(10)
    protected boolean unusualCategory = true;

    @Tag(11)
    protected boolean einvoices = true;

    @Tag(12)
    private boolean fraud = true;

    @Tag(13)
    private boolean leftToSpend = false;

    @Tag(14)
    private boolean loanUpdate = true;

    @ApiModelProperty(name = "balance", value="Indicates if the user wants to receive notifications with low or high balances alerts.", required=true)
    public boolean getBalance() {
        return balance;
    }

    @ApiModelProperty(name = "doubleCharge", value="Indicates if the user wants to receive notifications with double-charge alerts.", required=true)
    public boolean getDoubleCharge() {
        return doubleCharge;
    }

    @ApiModelProperty(name = "income", value="Indicates if the user wants to receive notifications when an income is received.", required=true)
    public boolean getIncome() {
        return income;
    }

    @ApiModelProperty(name = "largeExpense", value="Indicates if the user wants to receive notifications when a large expense is detected.", required=true)
    public boolean getLargeExpense() {
        return largeExpense;
    }

    @ApiModelProperty(name = "budget", value="Indicates if the user wants to receive notifications regarding her budgets.", required=true)
    public boolean getBudget() {
        return budget;
    }

    @ApiModelProperty(name = "unusualAccount", value="Indicates if the user wants to receive notifications when there is unusual activity on any of her accounts.", required=true)
    public boolean getUnusualAccount() {
        return unusualAccount;
    }

    @ApiModelProperty(name = "unusualCategory", value="Indicates if the user wants to receive notifications when she has spend more than usual on something.", required=true)
    public boolean getUnusualCategory() {
        return unusualCategory;
    }

    @ApiModelProperty(name = "transaction", value="Indicates if the user wants to receive notifications for every transaction.", required=true)
    public boolean getTransaction() {
        return transaction;
    }

    @ApiModelProperty(name = "summaryWeekly", value="Indicates if the user wants to receive notifications with weekly summaries.", required=true)
    public boolean getSummaryWeekly() {
        return summaryWeekly;
    }

    @ApiModelProperty(name = "summaryMonthly", value="Indicates if the user wants to receive notifications with monthly summaries.", required=true)
    public boolean getSummaryMonthly() {
        return summaryMonthly;
    }

    @ApiModelProperty(name = "einvoices", value="Indicates if the user wants to receive notifications for e-invoices.", required=true)
    public boolean getEinvoices() {
        return einvoices;
    }

    @ApiModelProperty(name = "fraud", value="Indicates if the user wants to receive notifications for ID Control warnings.", required=true)
    public boolean isFraud() {
        return fraud;
    }

    @ApiModelProperty(name = "leftToSpend", value= "Indicates if the user wants to receive left to spend notifications.", required=true)
    public boolean getLeftToSpend() {
        return leftToSpend;
    }

    @ApiModelProperty(name = "loanUpdate", value="Indicates if the user wants to receive notifications for loan updates.", required=true)
    public boolean getLoanUpdate() {
        return loanUpdate;
    }

    public void setBalance(boolean balance) {
        this.balance = balance;
    }

    public void setDoubleCharge(boolean doubleCharge) {
        this.doubleCharge = doubleCharge;
    }

    public void setIncome(boolean income) {
        this.income = income;
    }

    public void setLargeExpense(boolean largeExpense) {
        this.largeExpense = largeExpense;
    }

    public void setBudget(boolean budget) {
        this.budget = budget;
    }

    public void setUnusualAccount(boolean unusualAccount) {
        this.unusualAccount = unusualAccount;
    }

    public void setUnusualCategory(boolean unusualCategory) {
        this.unusualCategory = unusualCategory;
    }

    public void setTransaction(boolean transaction) {
        this.transaction = transaction;
    }

    public void setSummaryWeekly(boolean summaryWeekly) {
        this.summaryWeekly = summaryWeekly;
    }

    public void setSummaryMonthly(boolean summaryMonthly) {
        this.summaryMonthly = summaryMonthly;
    }

    public void setEinvoices(boolean einvoices) {
        this.einvoices = einvoices;
    }

    public void setFraud(boolean fraud) {
        this.fraud = fraud;
    }

    public void setLeftToSpend(boolean leftToSpend) {
        this.leftToSpend = leftToSpend;
    }

    public void setLoanUpdate(boolean loanUpdate) {
        this.loanUpdate = loanUpdate;
    }
}
