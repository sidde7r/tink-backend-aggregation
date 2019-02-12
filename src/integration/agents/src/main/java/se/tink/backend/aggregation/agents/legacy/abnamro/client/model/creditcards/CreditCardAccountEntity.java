package se.tink.backend.aggregation.agents.abnamro.client.model.creditcards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditCardAccountEntity {
    private List<TransactionContainerEntity> transactions;

    private String productCode;

    private Double authorizedBalance;

    private Double creditLimit;

    private Double currentBalance;

    private String contractNumber;

    private Double creditLeftToUse;

    public List<TransactionContainerEntity> getTransactions() {
        if (transactions == null) {
            return Collections.emptyList();
        }

        return transactions;
    }

    public void setTransactions(List<TransactionContainerEntity> transactions) {
        this.transactions = transactions;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public Double getAuthorizedBalance() {
        return authorizedBalance;
    }

    public void setAuthorizedBalance(Double authorizedBalance) {
        this.authorizedBalance = authorizedBalance;
    }

    public Double getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(Double creditLimit) {
        this.creditLimit = creditLimit;
    }

    public Double getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(Double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public Double getCreditLeftToUse() {
        return creditLeftToUse;
    }

    public void setCreditLeftToUse(Double creditLeftToUse) {
        this.creditLeftToUse = creditLeftToUse;
    }
}
