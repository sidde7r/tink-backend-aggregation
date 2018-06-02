package se.tink.backend.export.model.submodels;

public class ExportNotificationSettings {

    private final String balance;
    private final String budget;
    private final String doubleCharge;
    private final String income;
    private final String largeExpense;
    private final String summaryMonthly;
    private final String summaryWeekly;
    private final String transaction;
    private final String unusualAccount;
    private final String unusualCategory;
    private final String einvoices;
    private final String fraud;
    private final String leftToSpend;
    private final String loanUpdate;

    public ExportNotificationSettings(boolean balance, boolean budget, boolean doubleCharge, boolean income,
            boolean largeExpense, boolean summaryMonthly, boolean summaryWeekly, boolean transaction,
            boolean unusualAccount, boolean unusualCategory, boolean einvoices, boolean fraud, boolean leftToSpend,
            boolean loanUpdate) {
        this.balance = Boolean.toString(balance);
        this.budget = Boolean.toString(budget);
        this.doubleCharge = Boolean.toString(doubleCharge);
        this.income = Boolean.toString(income);
        this.largeExpense = Boolean.toString(largeExpense);
        this.summaryMonthly = Boolean.toString(summaryMonthly);
        this.summaryWeekly = Boolean.toString(summaryWeekly);
        this.transaction = Boolean.toString(transaction);
        this.unusualAccount = Boolean.toString(unusualAccount);
        this.unusualCategory = Boolean.toString(unusualCategory);
        this.einvoices = Boolean.toString(einvoices);
        this.fraud = Boolean.toString(fraud);
        this.leftToSpend = Boolean.toString(leftToSpend);
        this.loanUpdate = Boolean.toString(loanUpdate);
    }

    public String getBalance() {
        return balance;
    }

    public String getBudget() {
        return budget;
    }

    public String getDoubleCharge() {
        return doubleCharge;
    }

    public String getIncome() {
        return income;
    }

    public String getLargeExpense() {
        return largeExpense;
    }

    public String getSummaryMonthly() {
        return summaryMonthly;
    }

    public String getSummaryWeekly() {
        return summaryWeekly;
    }

    public String getTransaction() {
        return transaction;
    }

    public String getUnusualAccount() {
        return unusualAccount;
    }

    public String getUnusualCategory() {
        return unusualCategory;
    }

    public String getEinvoices() {
        return einvoices;
    }

    public String getFraud() {
        return fraud;
    }

    public String getLeftToSpend() {
        return leftToSpend;
    }

    public String getLoanUpdate() {
        return loanUpdate;
    }
}
