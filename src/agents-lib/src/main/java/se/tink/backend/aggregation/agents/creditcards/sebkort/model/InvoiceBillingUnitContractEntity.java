package se.tink.backend.aggregation.agents.creditcards.sebkort.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceBillingUnitContractEntity {
    protected String unInvoicedAmount;
    protected String contractId;
    protected String contractName;
    protected String creditAmount;

    public String getUnInvoicedAmount() {
        return unInvoicedAmount;
    }

    public void setUnInvoicedAmount(String unInvoicedAmount) {
        this.unInvoicedAmount = unInvoicedAmount;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public String getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(String creditAmount) {
        this.creditAmount = creditAmount;
    }

}
