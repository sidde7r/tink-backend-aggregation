package se.tink.backend.aggregation.agents.creditcards.sebkort.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceBillingUnitEntity {
    protected String billingUnitId;
    protected String billingUnitName;
    protected String arrangementId;
    protected String arrangementNumber;
    protected String cutOffDate;
    protected String nextInvoiceDate;
    protected String creditAmount;
    protected boolean showCreditAmount;
    protected boolean showInvoices;
    protected String unInvoicedAmount;
    protected String balance;
    protected List<InvoiceBillingUnitContractEntity> contracts;

    public List<InvoiceBillingUnitContractEntity> getContracts() {
        return contracts;
    }

    public void setContracts(List<InvoiceBillingUnitContractEntity> contracts) {
        this.contracts = contracts;
    }

    public String getBillingUnitId() {
        return billingUnitId;
    }

    public void setBillingUnitId(String billingUnitId) {
        this.billingUnitId = billingUnitId;
    }

    public String getBillingUnitName() {
        return billingUnitName;
    }

    public void setBillingUnitName(String billingUnitName) {
        this.billingUnitName = billingUnitName;
    }

    public String getArrangementId() {
        return arrangementId;
    }

    public void setArrangementId(String arrangementId) {
        this.arrangementId = arrangementId;
    }

    public String getArrangementNumber() {
        return arrangementNumber;
    }

    public void setArrangementNumber(String arrangementNumber) {
        this.arrangementNumber = arrangementNumber;
    }

    public String getCutOffDate() {
        return cutOffDate;
    }

    public void setCutOffDate(String cutOffDate) {
        this.cutOffDate = cutOffDate;
    }

    public String getNextInvoiceDate() {
        return nextInvoiceDate;
    }

    public void setNextInvoiceDate(String nextInvoiceDate) {
        this.nextInvoiceDate = nextInvoiceDate;
    }

    public String getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(String creditAmount) {
        this.creditAmount = creditAmount;
    }

    public boolean isShowCreditAmount() {
        return showCreditAmount;
    }

    public void setShowCreditAmount(boolean showCreditAmount) {
        this.showCreditAmount = showCreditAmount;
    }

    public boolean isShowInvoices() {
        return showInvoices;
    }

    public void setShowInvoices(boolean showInvoices) {
        this.showInvoices = showInvoices;
    }

    public String getUnInvoicedAmount() {
        return unInvoicedAmount;
    }

    public void setUnInvoicedAmount(String unInvoicedAmount) {
        this.unInvoicedAmount = unInvoicedAmount;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
}
