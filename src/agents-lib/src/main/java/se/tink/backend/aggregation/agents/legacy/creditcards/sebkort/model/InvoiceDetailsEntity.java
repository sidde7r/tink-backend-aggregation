package se.tink.backend.aggregation.agents.creditcards.sebkort.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceDetailsEntity {
    private String arrangementNumber;
    protected String balanceDate;
    protected String balanceDateIn;
    protected String bankAccountnumber;
    protected List<CardGroupEntity> cobrandCardGroups;
    protected List<CardGroupEntity> cardGroups;
    protected String creditAmount;
    protected String dueDate;
    protected boolean financeType;
    protected String invoiceAmount;
    protected String invoiceBalanceIn;
    protected String invoiceBalanceOut;
    protected String invoiceDate;
    protected String invoiceId;
    protected String invoiceNumber;
    protected String lowestInvoiceAmount;
    protected String ocr;
    protected String productName;
    protected String status;
    protected SummaryTransactionsEntity summaryTransactions;
    // May be avalible on pending
    protected List<TransactionGroupEntity> transactionGroups;

    public String getArrangementNumber() {
        return arrangementNumber;
    }

    public void setArrangementNumber(String arrangementNumber) {
        this.arrangementNumber = arrangementNumber;
    }

    public String getBalanceDate() {
        return balanceDate;
    }

    public String getBalanceDateIn() {
        return balanceDateIn;
    }

    public String getBankAccountnumber() {
        return bankAccountnumber;
    }

    public List<CardGroupEntity> getCobrandCardGroups() {
        return cobrandCardGroups;
    }

    public List<CardGroupEntity> getCardGroups() {
        return cardGroups;
    }

    public String getCreditAmount() {
        return creditAmount;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getInvoiceAmount() {
        return invoiceAmount;
    }

    public String getInvoiceBalanceIn() {
        return invoiceBalanceIn;
    }

    public String getInvoiceBalanceOut() {
        return invoiceBalanceOut;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public String getLowestInvoiceAmount() {
        return lowestInvoiceAmount;
    }

    public String getOcr() {
        return ocr;
    }

    public String getProductName() {
        return productName;
    }

    public String getStatus() {
        return status;
    }

    public SummaryTransactionsEntity getSummaryTransactions() {
        return summaryTransactions;
    }

    public List<TransactionGroupEntity> getTransactionGroups() {
        return transactionGroups;
    }

    public boolean isFinanceType() {
        return financeType;
    }

    public void setBalanceDate(String balanceDate) {
        this.balanceDate = balanceDate;
    }

    public void setBalanceDateIn(String balanceDateIn) {
        this.balanceDateIn = balanceDateIn;
    }

    public void setBankAccountnumber(String bankAccountnumber) {
        this.bankAccountnumber = bankAccountnumber;
    }

    public void setCobrandCardGroups(List<CardGroupEntity> cobrandCardGroups) {
        this.cobrandCardGroups = cobrandCardGroups;
    }

    public void setCardGroups(List<CardGroupEntity> cardGroups) {
        this.cardGroups = cardGroups;
    }

    public void setCreditAmount(String creditAmount) {
        this.creditAmount = creditAmount;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setFinanceType(boolean financeType) {
        this.financeType = financeType;
    }

    public void setInvoiceAmount(String invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public void setInvoiceBalanceIn(String invoiceBalanceIn) {
        this.invoiceBalanceIn = invoiceBalanceIn;
    }

    public void setInvoiceBalanceOut(String invoiceBalanceOut) {
        this.invoiceBalanceOut = invoiceBalanceOut;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public void setLowestInvoiceAmount(String lowestInvoiceAmount) {
        this.lowestInvoiceAmount = lowestInvoiceAmount;
    }

    public void setOcr(String ocr) {
        this.ocr = ocr;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSummaryTransactions(SummaryTransactionsEntity summaryTransactions) {
        this.summaryTransactions = summaryTransactions;
    }

    public void setTransactionGroups(List<TransactionGroupEntity> transactionGroups) {
        this.transactionGroups = transactionGroups;
    }
}
