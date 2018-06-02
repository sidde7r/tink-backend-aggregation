package se.tink.backend.aggregation.agents.creditcards.sebkort.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceEntity {
    protected String invoiceId;
    protected String productName;
    protected String invoiceNumber;
    protected String invoiceAmount;
    protected String lowestInvoiceAmount;
    protected String bankAccountNumber;
    protected String invoiceBalanceIn;
    protected String invoiceBalanceOut;
    protected String invoiceDate;
    protected String dueDate;
    protected String ocr;
    protected String status;
    protected boolean financeType;
    protected String creditAmount;
    protected String balanceDate;
    protected String balanceDateIn;

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(String invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public String getLowestInvoiceAmount() {
        return lowestInvoiceAmount;
    }

    public void setLowestInvoiceAmount(String lowestInvoiceAmount) {
        this.lowestInvoiceAmount = lowestInvoiceAmount;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getInvoiceBalanceIn() {
        return invoiceBalanceIn;
    }

    public void setInvoiceBalanceIn(String invoiceBalanceIn) {
        this.invoiceBalanceIn = invoiceBalanceIn;
    }

    public String getInvoiceBalanceOut() {
        return invoiceBalanceOut;
    }

    public void setInvoiceBalanceOut(String invoiceBalanceOut) {
        this.invoiceBalanceOut = invoiceBalanceOut;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getOcr() {
        return ocr;
    }

    public void setOcr(String ocr) {
        this.ocr = ocr;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isFinanceType() {
        return financeType;
    }

    public void setFinanceType(boolean financeType) {
        this.financeType = financeType;
    }

    public String getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(String creditAmount) {
        this.creditAmount = creditAmount;
    }

    public String getBalanceDate() {
        return balanceDate;
    }

    public void setBalanceDate(String balanceDate) {
        this.balanceDate = balanceDate;
    }

    public String getBalanceDateIn() {
        return balanceDateIn;
    }

    public void setBalanceDateIn(String balanceDateIn) {
        this.balanceDateIn = balanceDateIn;
    }
}
