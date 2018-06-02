package se.tink.backend.rpc;

public class UpdateNotificationSettingsCommand {
    private Boolean balance;
    private Boolean budget;
    private Boolean doubleCharge;
    private Boolean income;
    private Boolean largeExpense;
    private Boolean summaryMonthly;
    private Boolean summaryWeekly;
    private Boolean transaction;
    private Boolean unusualAccount;
    private Boolean unusualCategory;
    private Boolean einvoices;
    private Boolean fraud;
    private Boolean leftToSpend;
    private Boolean loanUpdate;

    public UpdateNotificationSettingsCommand() {
    }

    public UpdateNotificationSettingsCommand(Boolean balance, Boolean budget, Boolean doubleCharge,
            Boolean income, Boolean largeExpense, Boolean summaryMonthly, Boolean summaryWeekly,
            Boolean transaction, Boolean unusualAccount, Boolean unusualCategory, Boolean einvoices,
            Boolean fraud, Boolean leftToSpend, Boolean loanUpdate) {
        this.balance = balance;
        this.budget = budget;
        this.doubleCharge = doubleCharge;
        this.income = income;
        this.largeExpense = largeExpense;
        this.summaryMonthly = summaryMonthly;
        this.summaryWeekly = summaryWeekly;
        this.transaction = transaction;
        this.unusualAccount = unusualAccount;
        this.unusualCategory = unusualCategory;
        this.einvoices = einvoices;
        this.fraud = fraud;
        this.leftToSpend = leftToSpend;
        this.loanUpdate = loanUpdate;
    }

    public Boolean getBalance() {
        return balance;
    }

    public void setBalance(Boolean balance) {
        this.balance = balance;
    }

    public Boolean getBudget() {
        return budget;
    }

    public void setBudget(Boolean budget) {
        this.budget = budget;
    }

    public Boolean getDoubleCharge() {
        return doubleCharge;
    }

    public void setDoubleCharge(Boolean doubleCharge) {
        this.doubleCharge = doubleCharge;
    }

    public Boolean getIncome() {
        return income;
    }

    public void setIncome(Boolean income) {
        this.income = income;
    }

    public Boolean getLargeExpense() {
        return largeExpense;
    }

    public void setLargeExpense(Boolean largeExpense) {
        this.largeExpense = largeExpense;
    }

    public Boolean getSummaryMonthly() {
        return summaryMonthly;
    }

    public void setSummaryMonthly(Boolean summaryMonthly) {
        this.summaryMonthly = summaryMonthly;
    }

    public Boolean getSummaryWeekly() {
        return summaryWeekly;
    }

    public void setSummaryWeekly(Boolean summaryWeekly) {
        this.summaryWeekly = summaryWeekly;
    }

    public Boolean getTransaction() {
        return transaction;
    }

    public void setTransaction(Boolean transaction) {
        this.transaction = transaction;
    }

    public Boolean getUnusualAccount() {
        return unusualAccount;
    }

    public void setUnusualAccount(Boolean unusualAccount) {
        this.unusualAccount = unusualAccount;
    }

    public Boolean getUnusualCategory() {
        return unusualCategory;
    }

    public void setUnusualCategory(Boolean unusualCategory) {
        this.unusualCategory = unusualCategory;
    }

    public Boolean getEinvoices() {
        return einvoices;
    }

    public void setEinvoices(Boolean einvoices) {
        this.einvoices = einvoices;
    }

    public Boolean getFraud() {
        return fraud;
    }

    public void setFraud(Boolean fraud) {
        this.fraud = fraud;
    }

    public Boolean getLeftToSpend() {
        return leftToSpend;
    }

    public void setLeftToSpend(Boolean leftToSpend) {
        this.leftToSpend = leftToSpend;
    }

    public Boolean getLoanUpdate() {
        return loanUpdate;
    }

    public void setLoanUpdate(Boolean loanUpdate) {
        this.loanUpdate = loanUpdate;
    }
}
